(ns photo-mosaic.kd-tree-test
  (:require [clojure.test :refer :all]
            [photo-mosaic.test-utils :refer :all]
            [photo-mosaic.kd-tree :refer :all]))

;; http://en.wikipedia.org/wiki/K-d_tree
(def points '([2 3] [5 4] [9 6] [4 7] [8 1] [7 2]))

(deftest distance-functions
  (testing "euclidean distance"
    (are [a b d] (float= (euclidean-distance a b) d 0.01)
         [5 5 5] [5 5 5] 0
         [5 5 5] [5 5 4] 1
         [4 5 4] [5 4 5] 1.73
         [5 4 5] [4 5 4] 1.73)))

(deftest kd-tree-implementation
  (let [tree (kd-tree points)]
    (testing "root node"
      (is (= [7 2] (.loc tree))))

    (testing "root->left"
      (is (= [5 4] (.loc (.left tree)))))

    (testing "root->right"
      (is (= [9 6] (.loc (.right tree)))))

    (testing "root->left->left"
      (is (= [2 3] (.loc (.left (.left tree))))))

    (testing "root->left->right"
      (is (= [4 7] (.loc (.right (.left tree))))))

    (testing "root->right->left"
      (is (= [8 1] (.loc (.left (.right tree))))))

    (testing "root->right->right"
      (is (nil? (.right (.right tree)))))

    (testing "root->left->right->left"
      (is (nil? (.left (.right (.left tree))))))

    (testing "root->left->right->right"
      (is (nil? (.right (.right (.left tree))))))))

(deftest nearest-neighbor
  (let [tree (kd-tree points)]
    (testing "pick the closest point"
      (are [target loc] (= loc (closest-point tree target))
           [2 8] [4 7]
           [1 1] [2 3]
           [6 5] [5 4]
           [7 0] [8 1]
           [5 1] [7 2]
           [8 5] [9 6]))))
