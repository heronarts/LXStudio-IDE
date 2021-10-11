package heronarts.lx.app.media;

import java.io.FileFilter;
import java.util.Arrays;

import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import gifAnimation.Gif;

public class GifLibrary extends MediaLibrary<Gif> {
  @Override
  public Gif prepareMedia(String name) {
    Gif result = media.get(name);
    if (result != null) {
      return result;
    }
    String fullPath = listing.get(name);
    if (fullPath == null) {
      throw new IllegalArgumentException(String.format(
        "name %s not in listing: %s", name, listing.keySet().toString()));
    }

    try {
      result = new Gif(LXStudioApp.instance, fullPath);
      result.loop();
      media.put(name, result);
    } catch(Exception e) {
      logger.warning(e.toString());
    }
    return result;
  }

  public void init(LX lx) {
    String mediaFolder = "gifs";
    FileFilter filter = getFileFilterForExtensions(
      Arrays.asList("gif"));
    super.init(lx, mediaFolder, filter);
  }
}
