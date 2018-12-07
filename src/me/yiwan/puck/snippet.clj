(ns me.yiwan.puck.snippet
  (:require [clojure.java.io :refer [file]]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.conf :refer [conf]]
            [mount.core :refer [defstate]]
            [net.cgrand.enlive-html :as html]))

;; (defstate snippet
;;   :start {
;;           :head (html/defsnippet
;;                   head "snippets/header.html"
;;                   [:head] 
;;                   [title]
;;                   [:title] (html/content title))})
