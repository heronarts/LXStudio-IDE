package heronarts.lx.app.media;

import java.io.FileFilter;
import java.util.Arrays;

import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import processing.core.PFont;

public class FontLibrary extends MediaLibrary<PFont> {
  public static final int DEFAULT_FONT_SIZE = 96;

  public PFont prepareMedia(String name, int size) {
    String fullPath = listing.get(name);
    if (fullPath == null) {
      throw new IllegalArgumentException(String.format(
        "name %s not in listing: %s", name, listing.keySet().toString()));
    }
    String[] splitResult = splitExt(fullPath);
    if (splitResult[1] != "vlw") {
      name = String.join(".", String.format("%s-%d", splitResult[0], size),
        "vlw");
    }
    PFont result = media.get(name);
    if (result != null) {
      return result;
    }
    if (splitResult[1] != "vlw") {
      result = LXStudioApp.instance.createFont(fullPath, size);
    } else {
      result = LXStudioApp.instance.loadFont(fullPath);
    }
    media.put(name, result);
    return result;
  }

  @Override
  public PFont prepareMedia(String name) {
    return prepareMedia(name, DEFAULT_FONT_SIZE);
  }

  public void init(LX lx) {
    String mediaFolder = "fonts";
    FileFilter filter = getFileFilterForExtensions(
      Arrays.asList("ttf", "otf", "vlw"));
    super.init(lx, mediaFolder, filter);
  }
}
