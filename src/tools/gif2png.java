package tools;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class gif2png {

    public static void main(String[] args) throws IOException {
        // 输入 GIF 文件
        File gifFile = new File("E:/kof_swing/src/images/Chris/lj.gif"); // 改成你的路径
        // 输出目录
        File outDir = new File("E:/kof_swing/src/images/Chris/LJ");
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        // 创建 ImageInputStream
        try (ImageInputStream stream = ImageIO.createImageInputStream(gifFile)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw new RuntimeException("没有找到支持 GIF 的 ImageReader");
            }

            ImageReader reader = readers.next();
            reader.setInput(stream);

            int numFrames = reader.getNumImages(true); // 帧数
            System.out.println("总帧数: " + numFrames);

            for (int i = 0; i < numFrames; i++) {
                BufferedImage frame = reader.read(i);
                File out = new File(outDir, "frame_" + i + ".png");
                ImageIO.write(frame, "png", out);
                System.out.println("导出帧: " + out.getAbsolutePath());
            }

            reader.dispose();
        }
    }
}