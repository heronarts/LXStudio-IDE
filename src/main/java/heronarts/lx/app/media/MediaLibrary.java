package heronarts.lx.app.media;

import heronarts.lx.LX;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

public abstract class MediaLibrary<T> {
  protected HashMap<String, String> listing = new HashMap<String, String>();
  protected HashMap<String, T> media = new HashMap<String, T>();
  protected static final boolean PREPARE_MEDIA = false;

  protected static final Logger logger = Logger
    .getLogger(MediaLibrary.class.getName());

  public static String[] splitExt(String fileName) {
    String[] result = fileName.split("\\.(?=[^\\.]+$)");
    if (result.length > 0) {
      return result;
    }
    result = new String[] { fileName, "" };
    return result;
  }

  public static FileFilter getFileFilterForExtensions(List<String> extensions) {
    return new FileFilter() {
      @Override
      public boolean accept(File file) {
        return extensions.contains(splitExt(file.getName())[1]);
      }
    };
  }

  public static String getCanonicalContentPath(LX lx,
    String contentSubdirectory) {
    String contentPath = String.format("Content/%s/", contentSubdirectory);
    try {
      contentPath = lx.getMediaFolder(LX.Media.CONTENT).getCanonicalPath()
        + String.format("/%s/", contentSubdirectory);
    } catch (IOException e) {
      logger
        .severe(String.format("failed to get media folder: %s", e.toString()));
    }
    return contentPath;
  }

  abstract public T prepareMedia(String name);

  public void init(LX lx, String mediaFolder, FileFilter filter) {
    listing.clear();
    String contentPath = getCanonicalContentPath(lx, mediaFolder);
    File dir = new File(contentPath);
    File[] directoryListing = dir.listFiles(filter);
    if (directoryListing != null) {
      for (File child : directoryListing) {
        listing.put(child.getName(), child.getAbsolutePath());
        if (PREPARE_MEDIA) {
          prepareMedia(child.getName());
        }
      }
    }
    logger.info(String.format("%s listing: %s", mediaFolder,
      listing.keySet().toString()));
  }

  public String[] getNames(Predicate<String> predicate) {
    String[] names = listing.keySet().stream().filter(predicate).toArray(String[]::new);
    Arrays.sort(names);
    return names;
  }

  public String[] getNames() {
    return getNames((String name) -> true);
  }

}
