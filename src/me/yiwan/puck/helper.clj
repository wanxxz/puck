(ns me.yiwan.puck.helper
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.set :refer [subset?]]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as test]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.conf :refer [conf]]
            [me.yiwan.puck.markdown :refer [generate-blocks parse-content parse-meta meta-seq meta-map]]
            [mount.core :refer [defstate]]))

(defn list-post
  "return a lazy-seq of hash-map which contains title, date and content of a post"
  [& ks]
  (let [s (set ks)
        m (map #(hash-map :title (-> % slurp parse-meta meta-seq meta-map :title)
                          :date (re-find #"[0-9]{4}-[0-9]{2}-[0-9]{2}" (fs/name %))
                          :file (fs/name %)
                          :content (-> % slurp parse-content generate-blocks))
               (fs/find-files (.getPath (io/file (:wd conf)
                                                 (-> conf :dir :post)))
                              #".*\.md$"))
        m (->> (sort-by :date m)
               reverse
               (map #(->> (string/replace (:date %) #"-" "/")
                          (java.util.Date.)
                          (.format (java.text.SimpleDateFormat. "MMM d, y"))
                          (assoc % :date))))]
    (case s
      #{:title :date :content} m
      #{:title :content} (map #(hash-map :title (:title %) :file (:file %) :content (:content %)) m)
      #{:title :date} (map #(hash-map :title (:title %) :date (:date %) (:file %)) m)
      #{:date :content} (map #(hash-map :date (:date %) :file (:file %) :content (:content %)) m)
      #{:title} (map #(hash-map :title (:title %) :file (:file %)) m)
      #{:date} (map #(hash-map :date (:date %) :file (:file %)) m)
      #{:content} (map #(hash-map :file (:file %) :content (:content %)) m)
      (map #(hash-map :title (:title %) :date (:date %) :file (:file %)) m))))

(s/fdef list-post
  :args (s/or :ks nil?
              :ks #(subset? % #{:title :date :content})))

(comment
  (test/instrument `list-post))
