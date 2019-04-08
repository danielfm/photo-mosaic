(defproject photo-mosaic "0.0.1"
  :description "Experiment in creating photo mosaics in Clojure."
  :url "https://github.com/danielfm/photo-mosaic"
  :license {:name "BSD 2-Clause License"
            :url "http://opensource.org/licenses/BSD-2-Clause"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.cli "0.4.2"]]
  :aot :all
  :main photo-mosaic.core)
