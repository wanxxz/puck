(ns me.yiwan.puck.conf
  (:require [cprop.core :refer [load-config]]
            [mount.core :refer [defstate args]]))

(defstate conf :start (let [c (load-config :resource "conf.edn")]
                        (assoc c :wd (:working-directory (args)))))
