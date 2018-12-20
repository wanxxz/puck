(ns me.yiwan.puck.generate
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [mount.core :refer [defstate]]
            [me.yiwan.puck.conf :refer [conf]]
            [me.yiwan.puck.html :refer [generate-html]]))

(defn find-files
  [dir pattern]
  (let [path (.getPath (io/file (:wd conf) (-> conf :dir dir)))]
    (fs/find-files path pattern)))

(defn find-output-file
  [dir name ext]
  (let [output-file (io/file
                     (:wd conf)
                     (-> conf :dir :root)
                     (-> conf :dir dir)
                     (str name ".html"))]
    (if (-> output-file fs/file? not)
      (.createNewFile output-file))
    output-file))

(defn generate-content
  [dir]
  (doseq [input-file (find-files dir #".*\.md$")]
    (let [input-file-name (fs/base-name input-file true)
          output-file (find-output-file dir input-file-name ".html")]
      (spit output-file (generate-html (slurp input-file)))
      (println (format "generate: %s" (.getPath output-file))))))

(defstate generate :start {:page (generate-content :page)
                           :post (generate-content :post)})
