package com.dvoraksw.ce;

import jakarta.activation.CommandMap;
import jakarta.activation.DataHandler;
import jakarta.activation.MailcapCommandMap;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class SmtpEmail {

  public static void sendTextMessage(
      String from,
      List<String> to,
      List<String> cc,
      List<String> bcc,
      String subject,
      String text,
      Map<String, InputStream> attachments,
      Properties properties,
      String username,
      String password) {
    try {
      // Setting email command map
      var map = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
      map.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
      map.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
      map.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
      map.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
      map.addMailcap(
          "message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");

      // Creating mime message
      var mime = new MimeMessage(Session.getInstance(properties));

      // Creating mime message subparts
      var multipart = new MimeMultipart();

      // Inserting text message to mime message subpart
      var textMimeBodyPart = new MimeBodyPart();
      textMimeBodyPart.setContent(text, "text/plain; charset=utf-8");
      multipart.addBodyPart(textMimeBodyPart);

      // Add attachments of the message to multipart related
      attachments
          .keySet()
          .forEach(
              (it) -> {
                try {
                  var mimeBodyPart = new MimeBodyPart();
                  var lastIndexOf = it.lastIndexOf(".");
                  var extension =
                      lastIndexOf != -1
                          ? it.substring(lastIndexOf + 1).toLowerCase(Locale.getDefault())
                          : "";
                  var mimeType = getMimeTypeByFileExt(extension);
                  var dataSource = new ByteArrayDataSource(attachments.get(it), mimeType);
                  mimeBodyPart.setDataHandler(new DataHandler(dataSource));
                  mimeBodyPart.setFileName(it);
                  mimeBodyPart.setDisposition(MimeBodyPart.ATTACHMENT);
                  multipart.addBodyPart(mimeBodyPart);
                } catch (IOException | MessagingException e) {
                  throw new RuntimeException(e);
                }
              });

      // Inserting email content to mime message
      mime.setContent(multipart);

      // Inserting email addresses and subject to mime message
      mime.setFrom(new InternetAddress(from));
      for (var address : to) {
        mime.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(address));
      }
      for (var address : cc) {
        mime.addRecipient(jakarta.mail.Message.RecipientType.CC, new InternetAddress(address));
      }
      for (var address : bcc) {
        mime.addRecipient(jakarta.mail.Message.RecipientType.BCC, new InternetAddress(address));
      }
      mime.setSubject(subject);

      // Sending message
      if (username == null && password == null) {
        Transport.send(mime);
      } else {
        Transport.send(mime, username, password);
      }

    } catch (MessagingException e) {
      // Handling errors
      throw new RuntimeException(e);
    }
  }

  public static void sendHtmlMessage(
      String from,
      List<String> to,
      List<String> cc,
      List<String> bcc,
      String subject,
      String text,
      String html,
      Map<String, InputStream> images,
      Map<String, InputStream> attachments,
      Properties properties,
      String username,
      String password) {
    try {
      // Setting email command map
      var map = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
      map.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
      map.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
      map.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
      map.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
      map.addMailcap(
          "message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");

      // Creating mime message
      var mime = new MimeMessage(Session.getInstance(properties));

      // Creating mime message subparts
      var multipartMixed = new MimeMultipart("mixed");
      var multipartAlternative = new MimeMultipart("alternative");
      var multipartRelated = new MimeMultipart("related");

      // Inserting text message to mime message subpart
      var textMimeBodyPart = new MimeBodyPart();
      textMimeBodyPart.setContent(text, "text/plain; charset=utf-8");
      multipartAlternative.addBodyPart(textMimeBodyPart);

      // Inserting html message to mime message subpart
      var htmlMimeBodyPart = new MimeBodyPart();
      htmlMimeBodyPart.setContent(html, "text/html; charset=utf-8");
      multipartRelated.addBodyPart(htmlMimeBodyPart);

      // Inserting images to mime message subpart
      images
          .keySet()
          .forEach(
              (it) -> {
                try {
                  var mimeBodyPart = new MimeBodyPart();
                  var lastIndexOf = it.lastIndexOf(".");
                  var extension =
                      lastIndexOf != -1
                          ? it.substring(lastIndexOf + 1).toLowerCase(Locale.getDefault())
                          : "";
                  var mimeType = getMimeTypeByFileExt(extension);
                  var dataSource = new ByteArrayDataSource(images.get(it), mimeType);
                  mimeBodyPart.setDataHandler(new DataHandler(dataSource));
                  mimeBodyPart.setHeader("Content-ID", "<" + it + ">");
                  mimeBodyPart.setDisposition(MimeBodyPart.INLINE);
                  multipartRelated.addBodyPart(mimeBodyPart);
                } catch (IOException | MessagingException e) {
                  throw new RuntimeException(e);
                }
              });

      // Add attachments of the message to multipart related
      attachments
          .keySet()
          .forEach(
              (it) -> {
                try {
                  var mimeBodyPart = new MimeBodyPart();
                  var lastIndexOf = it.lastIndexOf(".");
                  var extension =
                      lastIndexOf != -1
                          ? it.substring(lastIndexOf + 1).toLowerCase(Locale.getDefault())
                          : "";
                  var mimeType = getMimeTypeByFileExt(extension);
                  var dataSource = new ByteArrayDataSource(attachments.get(it), mimeType);
                  mimeBodyPart.setDataHandler(new DataHandler(dataSource));
                  mimeBodyPart.setFileName(it);
                  mimeBodyPart.setDisposition(MimeBodyPart.ATTACHMENT);
                  multipartRelated.addBodyPart(mimeBodyPart);
                } catch (IOException | MessagingException e) {
                  throw new RuntimeException(e);
                }
              });

      // Joining related mime message subpart with alternative mime message subpart
      var relatedMimeBodyPart = new MimeBodyPart();
      relatedMimeBodyPart.setContent(multipartRelated);
      multipartAlternative.addBodyPart(relatedMimeBodyPart);

      // Joining alternative mime message subpart with mixed mime message subpart
      var alternativeMimeBodyPart = new MimeBodyPart();
      alternativeMimeBodyPart.setContent(multipartAlternative);
      multipartMixed.addBodyPart(alternativeMimeBodyPart);

      // Inserting email content to mime message
      mime.setContent(multipartMixed);

      // Inserting email addresses and subject to mime message
      mime.setFrom(new InternetAddress(from));
      for (var address : to) {
        mime.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(address));
      }
      for (var address : cc) {
        mime.addRecipient(jakarta.mail.Message.RecipientType.CC, new InternetAddress(address));
      }
      for (var address : bcc) {
        mime.addRecipient(jakarta.mail.Message.RecipientType.BCC, new InternetAddress(address));
      }
      mime.setSubject(subject);

      // Sending message
      if (username == null && password == null) {
        Transport.send(mime);
      } else {
        Transport.send(mime, username, password);
      }

    } catch (MessagingException e) {
      // Handling errors
      throw new RuntimeException(e);
    }
  }

  private static String getMimeTypeByFileExt(String extension) {
    // Getting mime type by file extension
    return switch (extension) {
      case "aac" -> "audio/aac";
      case "abw" -> "application/x-abiword";
      case "arc" -> "application/x-freearc";
      case "avi" -> "video/x-msvideo";
      case "azw" -> "application/vnd.amazon.ebook";
      case "bin" -> "application/octet-stream";
      case "bmp" -> "image/bmp";
      case "bz" -> "application/x-bzip";
      case "bz2" -> "application/x-bzip2";
      case "csh" -> "application/x-csh";
      case "css" -> "text/css";
      case "csv" -> "text/csv";
      case "doc" -> "application/msword";
      case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
      case "eot" -> "application/vnd.ms-fontobject";
      case "epub" -> "application/epub+zip";
      case "gz" -> "application/gzip";
      case "gif" -> "image/gif";
      case "htm" -> "text/html";
      case "html" -> "text/html";
      case "ico" -> "image/vnd.microsoft.icon";
      case "ics" -> "text/calendar";
      case "jar" -> "application/java-archive";
      case "jpeg" -> "image/jpeg";
      case "jpg" -> "image/jpeg";
      case "js" -> "text/javascript";
      case "json" -> "application/json";
      case "jsonid" -> "application/ld+json";
      case "mid" -> "audio/midi";
      case "midi" -> "audio/midi";
      case "mjs" -> "text/javascript";
      case "mp3" -> "audio/mpeg";
      case "mpeg" -> "video/mpeg";
      case "mpkg" -> "application/vnd.apple.installer+xml";
      case "odp" -> "application/vnd.oasis.opendocument.presentation";
      case "ods" -> "application/vnd.oasis.opendocument.spreadsheet";
      case "odt" -> "application/vnd.oasis.opendocument.text";
      case "oga" -> "audio/ogg";
      case "ogv" -> "video/ogg";
      case "ogx" -> "application/ogg";
      case "opus" -> "audio/opus";
      case "otf" -> "font/otf";
      case "png" -> "image/png";
      case "pdf" -> "application/pdf";
      case "php" -> "application/x-httpd-php";
      case "ppt" -> "application/vnd.ms-powerpoint";
      case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
      case "rar" -> "application/vnd.rar";
      case "rtf" -> "application/rtf";
      case "sh" -> "application/x-sh";
      case "svg" -> "image/svg+xml";
      case "swf" -> "application/x-shockwave-flash";
      case "tar" -> "application/x-tar";
      case "tif" -> "image/tiff";
      case "tiff" -> "image/tiff";
      case "ts" -> "video/mp2t";
      case "ttf" -> "font/ttf";
      case "txt" -> "text/plain";
      case "vsd" -> "application/vnd.visio";
      case "wav" -> "audio/wav";
      case "weba" -> "audio/webm";
      case "webm" -> "video/webm";
      case "webmanifest" -> "application/manifest+json";
      case "webp" -> "image/webp";
      case "woff" -> "font/woff";
      case "woff2" -> "font/woff2";
      case "xhtml" -> "application/xhtml+xml";
      case "xls" -> "application/vnd.ms-excel";
      case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
      case "xml" -> "text/xml";
      case "xul" -> "application/vnd.mozilla.xul+xml";
      case "zip" -> "application/zip";
      default -> throw new RuntimeException("Nelze určit MIME z přípony!");
    };
  }
}
