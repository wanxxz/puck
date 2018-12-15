(ns me.yiwan.puck.conf
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [cprop.core :refer [load-config]]
            [mount.core :refer [args defstate]]))

(defstate conf :start (let [wd (:working-directory (args))
                            n "conf.edn"
                            f (io/file wd n)]
                        (if (fs/exists? f)
                          (load-config :resource n :file f :merge [{:wd wd}])
                          (load-config :resource n :merge [{:wd wd}]))))
