package heronarts.lx.app.media;

import java.io.FileFilter;
import java.util.Arrays;

import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import processing.video.Movie;

public class VideoLibrary extends MediaLibrary<Movie> {
  public static final float DEFAULT_MOVIE_VOLUME = 0.0f;

  public Movie prepareMedia(String name, float volume) {
    Movie result = media.get(name);
    if (result != null) {
      return result;
    }
    String fullPath = listing.get(name);
    if (fullPath == null) {
      throw new IllegalArgumentException(String.format(
        "name %s not in listing: %s", name, listing.keySet().toString()));
    }
    result = new Movie(LXStudioApp.instance, fullPath);
    result.volume(volume);
    result.loop();
    result.volume(volume);
    media.put(name, result);
    return result;
  }

  @Override
  public Movie prepareMedia(String name) {
    return prepareMedia(name, DEFAULT_MOVIE_VOLUME);
  }

  public void init(LX lx) {
    String mediaFolder = "videos";
    FileFilter filter = createFileFilter(
      // gif doesn't work :(
      Arrays.asList("mov", "mp4"));
    super.init(lx, mediaFolder, filter);
  }
}
