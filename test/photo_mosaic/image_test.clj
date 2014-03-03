(ns photo-mosaic.image-test
  (:import [java.io File])
  (:require [clojure.test :refer :all]
            [photo-mosaic.image :refer :all]))

(def ^:dynamic *img*)

(defn load-img [t]
  (binding [*img* (load-image (File. "img/img-01.jpg"))]
    (t)))

(use-fixtures :each load-img)

(deftest image-info
  (testing "get image width"
    (is (= 1024 (image-width *img*))))

  (testing "get image height"
    (is (= 768 (image-height *img*))))

  (testing "get color at a pixel"
    (is (= [13 20 12] (pixel-rgb *img* 10 10))))

  (testing "get average color"
    (is (= [62 92 54] (image-average-color *img* 10)))))

(deftest image-crop
  (testing "crop to squared dimensions"
    (let [cropped-img (crop-image-squared *img*)]
      (is (= 768 (image-width cropped-img) (image-height cropped-img)))
      (is (= [69 100 58]  (image-average-color cropped-img 10)))

      ;; Save the file to disk for manual verification
      (save-image cropped-img (File. "tmp/img-01-crop.jpg")))))

(deftest image-resize
  (testing "resize to half the size"
    (let [resized-img (resize-image *img* 512 384)]
      (is (= 512 (image-width resized-img)))
      (is (= 384 (image-height resized-img)))

      ;; Save the file to disk for manual verification
      (save-image resized-img (File. "tmp/img-01-half.jpg")))))
