(ns me.yiwan.puck.watch
  (:require [clojure.java.io :as io]
            [hawk.core :as hawk]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.conf :refer [conf]]
            [me.yiwan.puck.content :refer [generate-html]]
            [mount.core :refer [defstate]]))

(defn create-watcher
  [dir]
  (hawk/watch!
   [{:paths [(.getPath (io/file (:wd conf) (-> conf :dir dir)))]
     :filter hawk/file?
     :handler (fn [_ e]
                (println (format "%s" (:file e)))
                (let [f (io/file
                         (:wd conf)
                         (-> conf :dir :root)
                         (-> conf :dir dir)
                         (str (fs/base-name (:file e) true) ".html"))]
                  (if (not (fs/file? f)) (.createNewFile f))
                  (spit f (generate-html (slurp (:file e))))))}]))

(defstate watch
  :start {:post (create-watcher :post)
          :page (create-watcher :page)}

  :stop  (do
           (hawk/stop! (:post watch))
           (hawk/stop! (:page watch))))
