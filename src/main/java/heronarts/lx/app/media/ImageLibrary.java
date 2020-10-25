package heronarts.lx.app.media;

import java.io.FileFilter;
import java.util.Arrays;

import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import processing.core.PImage;

public class ImageLibrary extends MediaLibrary<PImage> {
  @Override
  public PImage prepareMedia(String name) {
    PImage result = media.get(name);
    if (result != null) {
      return result;
    }
    String fullPath = listing.get(name);
    if (fullPath == null) {
      throw new IllegalArgumentException(String.format(
        "name %s not in listing: %s", name, listing.keySet().toString()));
    }

    result = LXStudioApp.instance.loadImage(fullPath);
    media.put(name, result);
    return result;
  }

  public void init(LX lx) {
    String mediaFolder = "images";
    FileFilter filter = getFileFilterForExtensions(
      Arrays.asList("gif", "jpg", "tga", "png"));
    super.init(lx, mediaFolder, filter);
  }
}
