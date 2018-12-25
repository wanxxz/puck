(ns me.yiwan.puck.markdown
  (:require [hiccup.core :as hiccup]
            [instaparse.core :as insta]))

(def parser
  (insta/parser
   "<Post> = Meta Content
    <Meta> = (Metamarker Metaline+ Metamarker EOL)
    Metaline = Metakey Colon <Whitespace> Metavalue EOL
    Metakey = #'[0-9a-zA-Z_]+'
    Metavalue = Word (Whitespace Word)*
    <Metamarker> = <#'[-]{3}'> EOL
    <Colon> = <':'>
    <Space> = <' '>
    <Content> = (Paragraph | Header | List | Ordered | Rule | Quote)*
    Header = Line Headerline Blankline+
    <Headerline> = h1 | h2
    h1 = '='+
    h2 = '-'+
    List = Listline+ Blankline+
    Listline = Listmarker <Whitespace+> Word (Whitespace Word)* EOL
    <Listmarker> = <'+' | '*' | '-'>
    Ordered = Orderedline+ Blankline+
    Orderedline = Orderedmarker Whitespace* Word (Whitespace Word)* EOL
    <Orderedmarker> = <#'[0-9]+\\.'>
    Rule = Ruleline Blankline+
    <Ruleline> = <'+'+ | '*'+ | '-'+>
    Paragraph = Line+ Blankline+
    <Blankline> = Whitespace* EOL
    <Line> = (Word | Inline) (Whitespace (Word | Inline))* (EOL | Br)
    Quote = Quotemarker Quoteline+ Blankline+
    <Quotemarker> = <'> '>
    <Quoteline> = Word (Whitespace Word)* EOL
    <Whitespace> = ' '+
    <Word> = #'\\S+'
    <Inline> = Link | Img | Em | Strong | Code | Del
    <Link> = #'\\[.+?\\]\\(.+?\\)'
    <Img> = #'!\\[.+?\\]\\(.+?\\)'
    <Em> = #'[*]{1}.+?[*]{1}'
    <Strong> = #'[*]{2}.+?[*]{2}'
    <Code> = #'[`]{2}.+?[`]{2}'
    <Del> = #'[~]{2}.+?[~]{2}'
    <Br> = '  \\n'
    <EOL> = <'\\n'>"))
 
(defn generate-inlines [str]
  (let [inlines [[#"!\[(.+?)\]\((.+?)\)" (fn [[n href]] (hiccup/html [:img {:src href :alt n}]))]
                 [#"\[(.+?)\]\((.+?)\)"  (fn [[n href]] (hiccup/html [:a {:href href} n]))]
                 [#"``(.+?)``"           (fn [s] (hiccup/html [:code s]))]
                 [#"\*\*(.+?)\*\*"       (fn [s] (hiccup/html [:strong s]))]
                 [#"\*(.+?)\*"           (fn [s] (hiccup/html [:em s]))]
                 [#"([\s]{2}[\n]{1})"    (fn [s] (hiccup/html [:br s]))]
                 [#"~~(.+?)~~"           (fn [s] (hiccup/html [:del s]))]]

        res (first (filter (complement nil?)
                           (for [[regex func] inlines]
                             (let [groups (re-matches regex str)]
                               (if groups (func (drop 1 groups)))))))]
    (if (nil? res) str res)))

(defn generate-blocks [blocks]
  (reduce str
          (for [b blocks]
            (case (first b)
              :Metaline ""
              :List (hiccup/html [:ul (for [li (drop 1 b)] [:li (apply str (map generate-inlines (drop 1 li)))])])
              :Ordered (hiccup/html [:ol (for [li (drop 1 b)] [:li (apply str (map generate-inlines (drop 1 li)))])])
              :Header (hiccup/html [(first (last b)) (apply str (map generate-inlines (take (- (count b) 2) (drop 1 b))))])
              :Rule (hiccup/html [:hr])
              :Paragraph (hiccup/html [:p (apply str (map generate-inlines (drop 1 b)))])
              :Quote (hiccup/html [:blockquote (reduce str (drop 1 b))])))))

(defn meta-seq
  "take a seq, return a lazy-seq of maps, contains meta key and value"
  [meta]
  (map #(hash-map (-> % second last keyword) (->> % last (drop 1) (into []) (reduce str))) meta))

(defn meta-map
  "take a lazy-seq, return a @map, contains meta key and value, use last one for duplicate keys"
  [s]
  (let [r (atom {})]
    (doseq [m s] (let [k (-> m keys last) v (-> m vals last)] (swap! r assoc k v)))
    @r))

(defn parse-meta
  [str]
  (let [res (parser str :start :Meta :partial true)]
    (if (insta/failure? res)
      (print res)
      res)))

(defn parse-content
  [str]
  (let [res (parser str)]
    (if (insta/failure? res)
      (print res)
      res)))
