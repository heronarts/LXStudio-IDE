// package heronarts.lx.app.media;

// import java.io.File;
// import java.io.FileFilter;
// import java.util.Arrays;

// import heronarts.lx.LX;
// import heronarts.lx.app.LXStudioApp;
// import processing.core.PApplet;
// import processing.opengl.PShader;

// public class ShaderLibrary extends MediaLibrary<PShader> {
//   @Override
//   public PShader prepareMedia(String name) {
//     PShader result = media.get(name);
//     if (result != null) {
//       return result;
//     }
//     String fullPath = listing.get(name);
//     if (fullPath == null) {
//       throw new IllegalArgumentException(String.format(
//         "name %s not in listing: %s", name, listing.keySet().toString()));
//     }
//     result = new PShader((PApplet)LXStudioApp.instance, fullPath);
//     result.volume(volume);
//     result.loop();
//     result.volume(volume);
//     media.put(name, result);
//     return result;
//   }

//   public void init(LX lx) {
//     String mediaFolder = "shaders";
//     FileFilter filter = new FileFilter() {
//       @Override
//       public boolean accept(File file) {
//         String[] splitResult = splitExt(file.getName());
//         boolean isGlsl = Arrays.asList("glsl").contains(splitResult[1]);
//         boolean isVertex = splitResult[0].contains("Vert");
//         return isGlsl && isVertex;
//       }
//     };
//     super.init(lx, mediaFolder, filter);
//   }
// }
