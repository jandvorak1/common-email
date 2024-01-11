package com.dvoraksw.ce;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class OutlookEmailTest {

  @Test
  void send() throws IOException {
    // Testing method send
    var bufferedImage = new BufferedImage(751, 469, BufferedImage.TYPE_INT_RGB);
    var graphics = bufferedImage.createGraphics();
    graphics.setBackground(Color.black);
    graphics.setColor(Color.green);
    graphics.fillRect(0, 0, 751, 469);
    var stream = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", stream);
    assertDoesNotThrow(
        () ->
            OutlookEmail.send(
                "example@example.com",
                List.of("example@example.com"),
                List.of(),
                List.of(),
                "Příliš žluťoučký kůň úpěl ďábelské ódy",
                "Příliš žluťoučký kůň úpěl ďábelské ódy",
                "<h1>Příliš žluťoučký kůň úpěl ďábelské ódy</h1><img src=cid:example.png>",
                Map.of("example.png", new ByteArrayInputStream(stream.toByteArray())),
                Map.of("example.png", new ByteArrayInputStream(stream.toByteArray()))));
  }
}
