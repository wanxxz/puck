(ns me.yiwan.puck.helper
  (:require [clojure.java.io :as io]
            [clojure.set :refer [subset?]]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as test]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.conf :refer [conf]]
            [me.yiwan.puck.markdown :refer [generate-blocks parse-content]]
            [mount.core :refer [defstate]]))

(defn list-post
  "return a lazy-seq of hash-map which contains title, date and content of a post"
  [& ks]
  (let [s (set ks)
        m (map #(hash-map :title (fs/name %)
                          :date (re-find #"[0-9]{4}-[0-9]{2}-[0-9]{2}" (fs/name %))
                          :content (-> % slurp parse-content generate-blocks))
               (fs/find-files (.getPath (io/file (:wd conf)
                                                 (-> conf :dir :post)))
                              #".*\.md$"))]
    (cond
      (or (empty? s) (= s #{:title :date})) (map #(hash-map :title (:title %) :date (:date %)) m)
      (= s #{:title :date :content}) m
      (= s #{:title :content}) (map #(hash-map :title (:title %) :content (:content %)) m)
      (= s #{:date :content}) (map #(hash-map :date (:date %) :content (:content %)) m)
      (= s #{:title}) (map #(hash-map :title (:title %)) m)
      (= s #{:date}) (map #(hash-map :date (:date %)) m)
      (= s #{:content}) (map #(hash-map :content (:content %)) m))))

(s/fdef list-post
  :args (s/or :ks nil?
              :ks #(subset? % #{:title :date :content}))) 

(comment
  (test/instrument `list-post))
