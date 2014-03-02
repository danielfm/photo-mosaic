(ns photo-mosaic.image
  (:import [java.io File]
           [javax.imageio ImageIO]
           [java.awt Color Graphics2D RenderingHints]
           [java.awt.image BufferedImage]))

(defn ^BufferedImage new-image-rgb
  "Returns an empty image with the given size."
  [width height]
  (BufferedImage. width height BufferedImage/TYPE_INT_RGB))

(defn draw!
  "Draws src-image onto dst-img at the given coordinate."
  [^BufferedImage src-img ^BufferedImage dst-img x y w h]
  (doto (.createGraphics dst-img)
    (.drawImage src-img x y w h nil)
    (.dispose))
  dst-img)

(defn ^BufferedImage load-image
  "Loads an image file from disk."
  [^File img-file]
  (ImageIO/read img-file))

(defn save-image
  "Saves a BufferedImage to disk."
  [^BufferedImage img ^File img-file]
  (ImageIO/write img "jpg" img-file))

(defn image-width
  "Returns the image width."
  [^BufferedImage img]
  (.getWidth img))

(defn image-height
  "Returns the image height."
  [^BufferedImage img]
  (.getHeight img))

(defn image-coordinate-seq
  "Returns a lazy seq with all [x,y] coordinates for the given image."
  [^BufferedImage img]
  (for [x (range 0 (image-width img))
        y (range 0 (image-height img))]
    [x y]))

(defn pixel-rgb
  "Returns a vec containing the RGB components of the pixel at (x,y)."
  [^BufferedImage img x y]
  (let [color (Color. (.getRGB img x y))]
    [(.getRed color) (.getGreen color) (.getBlue color)]))

(defn image-average-color
  "Returns img's average color by sampling."
  [^BufferedImage img step-size]
  (let [xs (range 0 (image-width img) step-size)
        ys (range 0 (image-height img) step-size)
        total (* (count xs) (count ys))
        pixels (for [x xs y ys] (pixel-rgb img x y))]
    (vec (map #(int (/ % total)) (apply map + pixels)))))

(defn ^BufferedImage crop-image-squared
  "Returns a centered square crop of img."
  [^BufferedImage img]
  (let [width (image-width img)
        height (image-height img)
        min-size (min width height)
        max-size (max width height)
        offset (int (/ (- max-size min-size) 2))]
    (if (= min-size width)
      (.getSubimage img 0 offset min-size min-size)
      (.getSubimage img offset 0 min-size min-size))))

(defn ^BufferedImage resize-image
  "Returns a new img scaled to the given dimensions."
  [^BufferedImage img width height]
  (let [^BufferedImage out (BufferedImage. width height (.getType img))]
    (draw! img out 0 0 width height)))
