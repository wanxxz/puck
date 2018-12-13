(ns me.yiwan.puck.conf
  (:require [clojure.java.io :as io]
            [cprop.core :refer [load-config]]
            [mount.core :refer [args defstate]]))

(defstate conf :start (let [wd (:working-directory (args))]
                        (load-config :resource "conf.edn"
                                     :file (io/file wd "conf.edn")
                                     :merge [{:wd wd}]))) 
