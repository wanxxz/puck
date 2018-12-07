(ns me.yiwan.puck.check
  (:require [clojure.java.io :as io]
            [mount.core :refer [defstate]]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.conf :refer [conf]]))

(defn check-directories
  []
  (doseq [d (-> conf :dir vals)]
    (let [f (io/file (:wd conf) d)]
      (if (not (fs/directory? f))
        (println (format "missing directory: %s" (.getPath f)))))))

(defstate check :start (check-directories))
