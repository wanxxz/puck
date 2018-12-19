(ns me.yiwan.puck.snippet
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.conf :refer [conf]]
            [me.yiwan.puck.helper :refer [list-post]]
            [mount.core :refer [defstate args]]
            [net.cgrand.enlive-html :as enlive]
            net.cgrand.reload))

;; (net.cgrand.reload/auto-reload *ns*)

(def prefix "snippet-")

(def preset #{"head" "post-list"})

(defmacro find-snippet-files
  "Return a list of files, or one file with given name."
  [& {:keys [name] :as hm}]
  (let [ext ".html"
        pat #".*\.html$"
        w (:wd conf)
        d (-> conf :dir :snippet)
        wd (remove nil? [w d])
        n (:name hm)]
    (cond
      n `(let [f# (io/file ~@wd (str ~n ~ext))
               r# (-> f# .getPath io/resource)]
           ;; find snippet under working dir either resources dir
           (if (fs/file? f#) f# r#))
      :else `(-> (io/file ~@wd) .getPath (fs/find-files ~pat)))))

(defn create-snippet-function
  [name file]
  (if (contains? preset name)
    nil
    (intern
     'me.yiwan.puck.snippet
     (symbol (str prefix name))
     (enlive/snippet file
                     [#{:head :body} :> :*]
                     [& args]))))
(def snippet-post-list
  (enlive/snippet
   (find-snippet-files :name "post-list")
   [#{:ul :ol}]
   [& args]
   [#{:ul :ol} [:li enlive/first-of-type]]
   (enlive/clone-for [post (list-post)]
                     [:a] (enlive/set-attr :href
                                           (format "/%s/%s.html"
                                                   (-> conf :dir :post)
                                                   (:title post)))
                     [:a :span.title] (enlive/content (:title post))
                     [:a :span.date] (enlive/content (:date post))
                     [:a :span.content] (enlive/html-content (:content post)))))

(def snippet-head
  (enlive/snippet
   (find-snippet-files :name "head")
   [:head :*]
   [& {:keys [:meta] :as h}]
   [:title] (enlive/content (:title (:meta h)))))

(defstate snippet :start (doseq [f (find-snippet-files)]
                           (create-snippet-function (fs/base-name f true) f)))
