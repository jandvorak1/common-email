package com.dvoraksw.ce;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OutlookEmail {

  private static final String VBSCRIPT_FILE = "EmailSender.vbs";

  public static void send(
      String from,
      List<String> to,
      List<String> cc,
      List<String> bcc,
      String subject,
      String text,
      String html,
      Map<String, InputStream> images,
      Map<String, InputStream> attachments) {
    try {
      // Checking if platform is windows

      // Preparing data for visual basic script
      var paramTo = to.isEmpty() ? "" : String.join(";", to);
      var paramCc = cc.isEmpty() ? "" : String.join(";", cc);
      var paramBcc = bcc.isEmpty() ? "" : String.join(";", bcc);
      var paramText = text.isBlank() ? "" : text;
      var paramHtml = html.isBlank() ? "" : html;

      // Preparing visual basic script
      saveAssets(images);
      saveAssets(attachments);
      saveVBScript(
          createVBScript(
              new Params(
                  from,
                  paramTo,
                  paramCc,
                  paramBcc,
                  subject,
                  paramText,
                  paramHtml,
                  images.keySet().stream()
                      .map(
                          it ->
                              FileSystems.getDefault()
                                  .getPath(System.getProperty("java.io.tmpdir"), it)
                                  .toString())
                      .toList(),
                  attachments.keySet().stream()
                      .map(
                          it ->
                              FileSystems.getDefault()
                                  .getPath(System.getProperty("java.io.tmpdir"), it)
                                  .toString())
                      .toList())));

      // Sending email by run visual basic script
      var command =
          new String[] {
            "cmd",
            "/c",
            FileSystems.getDefault()
                .getPath(System.getProperty("java.io.tmpdir"), VBSCRIPT_FILE)
                .toString()
          };
      var process = Runtime.getRuntime().exec(command);
      process.waitFor();

      // Deleting visual basic script
      switch (process.exitValue()) {
        case 1 -> throw new RuntimeException("Odeslání emailu se nezdařilo!");
        case 2 -> throw new RuntimeException("Musí být spuštěný Microsoft Outlook!");
        default -> {
          deleteVBScript();
          deleteAssets(images);
          deleteAssets(attachments);
        }
      }
    } catch (IOException | InterruptedException e) {
      // Handling errors
      throw new RuntimeException(e);
    }
  }

  private static void saveVBScript(String text) throws IOException {
    // Saving visual basic script to temp directory in Windows 1250 charset
    var path =
        FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir"), VBSCRIPT_FILE);
    var utf8Text = text.getBytes(StandardCharsets.UTF_8);
    var cp1250Text = new String(utf8Text, StandardCharsets.UTF_8).getBytes("Windows-1250");
    Files.write(path, cp1250Text);
  }

  private static void deleteVBScript() {
    // Deleting visual basic script to temp directory
    try {
      Files.deleteIfExists(
          FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir"), VBSCRIPT_FILE));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void saveAssets(Map<String, InputStream> assets) {
    // Saving assets to temp directory
    assets.forEach(
        (k, v) -> {
          try {
            var path = FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir"), k);
            Files.deleteIfExists(path);
            try (var outputStream = new FileOutputStream(path.toFile(), false)) {
              v.transferTo(outputStream);
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }

  private static void deleteAssets(Map<String, InputStream> assets) {
    // Deleting assets from temp directory
    assets.forEach(
        (k, v) -> {
          try {
            Files.deleteIfExists(
                FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir"), k));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }

  private static String createVBScript(Params params) {
    // Creating visual basic script text from params
    return """
Option Explicit
On Error Resume Next

Dim Outlook, Message, Recipient, Item, Attachment

'-- Checking if MS Outlook is opened, if not exit with status code 2
Set Outlook = GetObject(, "Outlook.Application")
If Outlook Is Nothing Then WScript.Quit(2)

'-- Creating email
Set Message = Outlook.CreateItem(0)
Message.Sender = "{{from}}"
Message.To = "{{to}}"
Message.CC = "{{cc}}"
Message.BCC = "{{bcc}}"
Message.Subject = "{{subject}}"
Message.Body = "{{text}}"
Message.HTMLBody = "{{html}}"
{{images}}
{{attachments}}

'-- Sending email
Message.Send
If Err.Number <> 0 Then WScript.Quit(1)

'-- Exiting with status code 0
WScript.Quit(0)
        """
        .replace("{{from}}", params.from())
        .replace("{{to}}", params.to())
        .replace("{{cc}}", params.cc())
        .replace("{{bcc}}", params.bcc())
        .replace("{{subject}}", params.subject())
        .replace("{{text}}", params.text())
        .replace("{{html}}", params.html())
        .replace(
            "{{images}}",
            params.images().stream()
                .map(it -> String.format("Message.Attachments.Add(\"%s\")\n", it))
                .collect(Collectors.joining("")))
        .replace(
            "{{attachments}}",
            params.images().stream()
                .map(it -> String.format("Message.Attachments.Add(\"%s\")\n", it))
                .collect(Collectors.joining("")));
  }

  private record Params(
      String from,
      String to,
      String cc,
      String bcc,
      String subject,
      String text,
      String html,
      List<String> images,
      List<String> attachments) {}
}
