(ns photo-mosaic.core
  (:gen-class)
  (:import  [java.io File])
  (:require [photo-mosaic.image :refer :all]
            [photo-mosaic.kd-tree :refer :all]
            [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]))

;; Only supports JPEG files for now
(def file-ext #"(?i)\.jpg$")

(defn files-from-dir
  "Lists all files recursively from dir that matches regexp re."
  [re dir]
  (filter #(and (.isFile %) (re-find re (.getName %))) (file-seq dir)))

(defn process-tile
  "Crops and resizes img-file and saves it in out-dir as a squared image."
  [img-file size out-dir]
  (let [img (load-image img-file)
        out (File. (str (.getPath out-dir) File/separator (.getName img-file)))
        out-img (resize-image (crop-image-squared img) size size)
        avg-rgb (image-average-color out-img (/ size 10))]
    (save-image out-img out)
    [avg-rgb out]))

(defn process-jpg-dir
  "Saves all JPEG images in src-dir to squared tiles of the given size in out-dir."
  [tile-size src-dir out-dir]
  (let [in (File. src-dir)
        out (File. out-dir)]
    (into {} (pmap #(process-tile % tile-size out)
                   (files-from-dir file-ext in)))))

(defn usage [options-summary]
  (->> ["Photo Mosaic Generator"
        ""
        "Usage: photo-mosaic [options] INPUT-FILE"
        ""
        "Options:"
        options-summary
        ""]
       (string/join \newline)))

(defn error-msg
  "Returns a error message string with the given error messages."
  [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  "Exits the program with a message."
  (println msg)
  (System/exit status))

;; Command line options
(def cli-options
  [["-w" "--width N"  "Tile width" :parse-fn #(Integer/parseInt %)]
   ["-s" "--src DIR"  "Source directory with JPEG images"]
   ["-t" "--tmp DIR"  "Temporary directory"]
   ["-o" "--out FILE" "Output JPEG file path"]])

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
     (:help options) (exit 0 (usage summary))
     (not= (count arguments) 1) (exit 1 (usage summary))
     errors (exit 1 (error-msg errors)))
    (let [{:keys [src tmp width out]} options
          input-img (load-image (File. (first arguments)))
          out-img   (new-image-rgb (* width (image-width input-img))
                                   (* width (image-height input-img)))
          tile-data (process-jpg-dir width src tmp)
          palette-tree (kd-tree (keys tile-data))]
      (doall
       (pmap (fn [[x y]]
               (let [pixel (pixel-rgb input-img x y)
                     closest (load-image (tile-data (closest-point palette-tree pixel)))]
                 (draw! closest out-img (* x width) (* y width) width width)))
             (image-coordinate-seq input-img)))
      (save-image out-img (File. out))
      (shutdown-agents))))
