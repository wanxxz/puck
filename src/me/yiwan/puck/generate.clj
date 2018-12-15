(ns me.yiwan.puck.generate
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.conf :refer [conf]]
            [me.yiwan.puck.template :refer [template]]
            [me.yiwan.puck.content :refer [generate-html]]
            [mount.core :refer [defstate]]))

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
      (spit output-file (generate-html (slurp input-file))))))

(defstate generate :start (do
                            (generate-content :post)
                            (generate-content :page)))
