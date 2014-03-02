(ns photo-mosaic.test-utils)

(defn float=
  "Float number comparison."
  [a b delta]
  (<= (Math/abs (- a b)) delta))
