/**
 * Document   : myVerein.user
 * Description: The JavaScript used by the UserManagement page. The file includes list.js, list.js fuzzy search plugin and bootstrap datepicker.
 * Copyright  : (c) 2014 Frank Steiler <frank@steilerdev.de>
 * License    : GNU General Public License v2.0
 */

//List.js import
!function(){function a(b,c,d){var e=a.resolve(b);if(null==e){d=d||b,c=c||"root";var f=new Error('Failed to require "'+d+'" from "'+c+'"');throw f.path=d,f.parent=c,f.require=!0,f}var g=a.modules[e];if(!g._resolving&&!g.exports){var h={};h.exports={},h.client=h.component=!0,g._resolving=!0,g.call(this,h.exports,a.relative(e),h),delete g._resolving,g.exports=h.exports}return g.exports}a.modules={},a.aliases={},a.resolve=function(b){"/"===b.charAt(0)&&(b=b.slice(1));for(var c=[b,b+".js",b+".json",b+"/index.js",b+"/index.json"],d=0;d<c.length;d++){var b=c[d];if(a.modules.hasOwnProperty(b))return b;if(a.aliases.hasOwnProperty(b))return a.aliases[b]}},a.normalize=function(a,b){var c=[];if("."!=b.charAt(0))return b;a=a.split("/"),b=b.split("/");for(var d=0;d<b.length;++d)".."==b[d]?a.pop():"."!=b[d]&&""!=b[d]&&c.push(b[d]);return a.concat(c).join("/")},a.register=function(b,c){a.modules[b]=c},a.alias=function(b,c){if(!a.modules.hasOwnProperty(b))throw new Error('Failed to alias "'+b+'", it does not exist');a.aliases[c]=b},a.relative=function(b){function c(a,b){for(var c=a.length;c--;)if(a[c]===b)return c;return-1}function d(c){var e=d.resolve(c);return a(e,b,c)}var e=a.normalize(b,"..");return d.resolve=function(d){var f=d.charAt(0);if("/"==f)return d.slice(1);if("."==f)return a.normalize(e,d);var g=b.split("/"),h=c(g,"deps")+1;return h||(h=0),d=g.slice(0,h+1).join("/")+"/deps/"+d},d.exists=function(b){return a.modules.hasOwnProperty(d.resolve(b))},d},a.register("component-classes/index.js",function(a,b,c){function d(a){if(!a)throw new Error("A DOM element reference is required");this.el=a,this.list=a.classList}var e=b("indexof"),f=/\s+/,g=Object.prototype.toString;c.exports=function(a){return new d(a)},d.prototype.add=function(a){if(this.list)return this.list.add(a),this;var b=this.array(),c=e(b,a);return~c||b.push(a),this.el.className=b.join(" "),this},d.prototype.remove=function(a){if("[object RegExp]"==g.call(a))return this.removeMatching(a);if(this.list)return this.list.remove(a),this;var b=this.array(),c=e(b,a);return~c&&b.splice(c,1),this.el.className=b.join(" "),this},d.prototype.removeMatching=function(a){for(var b=this.array(),c=0;c<b.length;c++)a.test(b[c])&&this.remove(b[c]);return this},d.prototype.toggle=function(a,b){return this.list?("undefined"!=typeof b?b!==this.list.toggle(a,b)&&this.list.toggle(a):this.list.toggle(a),this):("undefined"!=typeof b?b?this.add(a):this.remove(a):this.has(a)?this.remove(a):this.add(a),this)},d.prototype.array=function(){var a=this.el.className.replace(/^\s+|\s+$/g,""),b=a.split(f);return""===b[0]&&b.shift(),b},d.prototype.has=d.prototype.contains=function(a){return this.list?this.list.contains(a):!!~e(this.array(),a)}}),a.register("segmentio-extend/index.js",function(a,b,c){c.exports=function(a){for(var b,c=Array.prototype.slice.call(arguments,1),d=0;b=c[d];d++)if(b)for(var e in b)a[e]=b[e];return a}}),a.register("component-indexof/index.js",function(a,b,c){c.exports=function(a,b){if(a.indexOf)return a.indexOf(b);for(var c=0;c<a.length;++c)if(a[c]===b)return c;return-1}}),a.register("component-event/index.js",function(a){var b=window.addEventListener?"addEventListener":"attachEvent",c=window.removeEventListener?"removeEventListener":"detachEvent",d="addEventListener"!==b?"on":"";a.bind=function(a,c,e,f){return a[b](d+c,e,f||!1),e},a.unbind=function(a,b,e,f){return a[c](d+b,e,f||!1),e}}),a.register("timoxley-to-array/index.js",function(a,b,c){function d(a){return"[object Array]"===Object.prototype.toString.call(a)}c.exports=function(a){if("undefined"==typeof a)return[];if(null===a)return[null];if(a===window)return[window];if("string"==typeof a)return[a];if(d(a))return a;if("number"!=typeof a.length)return[a];if("function"==typeof a&&a instanceof Function)return[a];for(var b=[],c=0;c<a.length;c++)(Object.prototype.hasOwnProperty.call(a,c)||c in a)&&b.push(a[c]);return b.length?b:[]}}),a.register("javve-events/index.js",function(a,b){var c=b("event"),d=b("to-array");a.bind=function(a,b,e,f){a=d(a);for(var g=0;g<a.length;g++)c.bind(a[g],b,e,f)},a.unbind=function(a,b,e,f){a=d(a);for(var g=0;g<a.length;g++)c.unbind(a[g],b,e,f)}}),a.register("javve-get-by-class/index.js",function(a,b,c){c.exports=function(){return document.getElementsByClassName?function(a,b,c){return c?a.getElementsByClassName(b)[0]:a.getElementsByClassName(b)}:document.querySelector?function(a,b,c){return b="."+b,c?a.querySelector(b):a.querySelectorAll(b)}:function(a,b,c){var d=[],e="*";null==a&&(a=document);for(var f=a.getElementsByTagName(e),g=f.length,h=new RegExp("(^|\\s)"+b+"(\\s|$)"),i=0,j=0;g>i;i++)if(h.test(f[i].className)){if(c)return f[i];d[j]=f[i],j++}return d}}()}),a.register("javve-get-attribute/index.js",function(a,b,c){c.exports=function(a,b){var c=a.getAttribute&&a.getAttribute(b)||null;if(!c)for(var d=a.attributes,e=d.length,f=0;e>f;f++)void 0!==b[f]&&b[f].nodeName===b&&(c=b[f].nodeValue);return c}}),a.register("javve-natural-sort/index.js",function(a,b,c){c.exports=function(a,b,c){var d,e,f=/(^-?[0-9]+(\.?[0-9]*)[df]?e?[0-9]?$|^0x[0-9a-f]+$|[0-9]+)/gi,g=/(^[ ]*|[ ]*$)/g,h=/(^([\w ]+,?[\w ]+)?[\w ]+,?[\w ]+\d+:\d+(:\d+)?[\w ]?|^\d{1,4}[\/\-]\d{1,4}[\/\-]\d{1,4}|^\w+, \w+ \d+, \d{4})/,i=/^0x[0-9a-f]+$/i,j=/^0/,c=c||{},k=function(a){return c.insensitive&&(""+a).toLowerCase()||""+a},l=k(a).replace(g,"")||"",m=k(b).replace(g,"")||"",n=l.replace(f,"\x00$1\x00").replace(/\0$/,"").replace(/^\0/,"").split("\x00"),o=m.replace(f,"\x00$1\x00").replace(/\0$/,"").replace(/^\0/,"").split("\x00"),p=parseInt(l.match(i))||1!=n.length&&l.match(h)&&Date.parse(l),q=parseInt(m.match(i))||p&&m.match(h)&&Date.parse(m)||null,r=c.desc?-1:1;if(q){if(q>p)return-1*r;if(p>q)return 1*r}for(var s=0,t=Math.max(n.length,o.length);t>s;s++){if(d=!(n[s]||"").match(j)&&parseFloat(n[s])||n[s]||0,e=!(o[s]||"").match(j)&&parseFloat(o[s])||o[s]||0,isNaN(d)!==isNaN(e))return isNaN(d)?1:-1;if(typeof d!=typeof e&&(d+="",e+=""),e>d)return-1*r;if(d>e)return 1*r}return 0}}),a.register("javve-to-string/index.js",function(a,b,c){c.exports=function(a){return a=void 0===a?"":a,a=null===a?"":a,a=a.toString()}}),a.register("component-type/index.js",function(a,b,c){var d=Object.prototype.toString;c.exports=function(a){switch(d.call(a)){case"[object Date]":return"date";case"[object RegExp]":return"regexp";case"[object Arguments]":return"arguments";case"[object Array]":return"array";case"[object Error]":return"error"}return null===a?"null":void 0===a?"undefined":a!==a?"nan":a&&1===a.nodeType?"element":typeof a.valueOf()}}),a.register("list.js/index.js",function(a,b,c){!function(a,d){"use strict";var e=a.document,f=b("get-by-class"),g=b("extend"),h=b("indexof"),i=function(a,c,i){var j,k=this,l=b("./src/item")(k),m=b("./src/add-async")(k),n=b("./src/parse")(k);j={start:function(){k.listClass="list",k.searchClass="search",k.sortClass="sort",k.page=200,k.i=1,k.items=[],k.visibleItems=[],k.matchingItems=[],k.searched=!1,k.filtered=!1,k.handlers={updated:[]},k.plugins={},k.helpers={getByClass:f,extend:g,indexOf:h},g(k,c),k.listContainer="string"==typeof a?e.getElementById(a):a,k.listContainer&&(k.list=f(k.listContainer,k.listClass,!0),k.templater=b("./src/templater")(k),k.search=b("./src/search")(k),k.filter=b("./src/filter")(k),k.sort=b("./src/sort")(k),this.items(),k.update(),this.plugins())},items:function(){n(k.list),i!==d&&k.add(i)},plugins:function(){for(var a=0;a<k.plugins.length;a++){var b=k.plugins[a];k[b.name]=b,b.init(k)}}},this.add=function(a,b){if(b)return m(a,b),void 0;var c=[],e=!1;a[0]===d&&(a=[a]);for(var f=0,g=a.length;g>f;f++){var h=null;a[f]instanceof l?(h=a[f],h.reload()):(e=k.items.length>k.page?!0:!1,h=new l(a[f],d,e)),k.items.push(h),c.push(h)}return k.update(),c},this.show=function(a,b){return this.i=a,this.page=b,k.update(),k},this.remove=function(a,b,c){for(var d=0,e=0,f=k.items.length;f>e;e++)k.items[e].values()[a]==b&&(k.templater.remove(k.items[e],c),k.items.splice(e,1),f--,e--,d++);return k.update(),d},this.get=function(a,b){for(var c=[],d=0,e=k.items.length;e>d;d++){var f=k.items[d];f.values()[a]==b&&c.push(f)}return c},this.size=function(){return k.items.length},this.clear=function(){return k.templater.clear(),k.items=[],k},this.on=function(a,b){return k.handlers[a].push(b),k},this.off=function(a,b){var c=k.handlers[a],d=h(c,b);return d>-1&&c.splice(d,1),k},this.trigger=function(a){for(var b=k.handlers[a].length;b--;)k.handlers[a][b](k);return k},this.reset={filter:function(){for(var a=k.items,b=a.length;b--;)a[b].filtered=!1;return k},search:function(){for(var a=k.items,b=a.length;b--;)a[b].found=!1;return k}},this.update=function(){var a=k.items,b=a.length;k.visibleItems=[],k.matchingItems=[],k.templater.clear();for(var c=0;b>c;c++)a[c].matching()&&k.matchingItems.length+1>=k.i&&k.visibleItems.length<k.page?(a[c].show(),k.visibleItems.push(a[c]),k.matchingItems.push(a[c])):a[c].matching()?(k.matchingItems.push(a[c]),a[c].hide()):a[c].hide();return k.trigger("updated"),k},j.start()};c.exports=i}(window)}),a.register("list.js/src/search.js",function(a,b,c){var d=b("events"),e=b("get-by-class"),f=b("to-string");c.exports=function(a){var b,c,g,h,i={resetList:function(){a.i=1,a.templater.clear(),h=void 0},setOptions:function(a){2==a.length&&a[1]instanceof Array?c=a[1]:2==a.length&&"function"==typeof a[1]?h=a[1]:3==a.length&&(c=a[1],h=a[2])},setColumns:function(){c=void 0===c?i.toArray(a.items[0].values()):c},setSearchString:function(a){a=f(a).toLowerCase(),a=a.replace(/[-[\]{}()*+?.,\\^$|#]/g,"\\$&"),g=a},toArray:function(a){var b=[];for(var c in a)b.push(c);return b}},j={list:function(){for(var b=0,c=a.items.length;c>b;b++)j.item(a.items[b])},item:function(a){a.found=!1;for(var b=0,d=c.length;d>b;b++)if(j.values(a.values(),c[b]))return a.found=!0,void 0},values:function(a,c){return a.hasOwnProperty(c)&&(b=f(a[c]).toLowerCase(),""!==g&&b.search(g)>-1)?!0:!1},reset:function(){a.reset.search(),a.searched=!1}},k=function(b){return a.trigger("searchStart"),i.resetList(),i.setSearchString(b),i.setOptions(arguments),i.setColumns(),""===g?j.reset():(a.searched=!0,h?h(g,c):j.list()),a.update(),a.trigger("searchComplete"),a.visibleItems};return a.handlers.searchStart=a.handlers.searchStart||[],a.handlers.searchComplete=a.handlers.searchComplete||[],d.bind(e(a.listContainer,a.searchClass),"keyup",function(b){var c=b.target||b.srcElement,d=""===c.value&&!a.searched;d||k(c.value)}),d.bind(e(a.listContainer,a.searchClass),"input",function(a){var b=a.target||a.srcElement;""===b.value&&k("")}),a.helpers.toString=f,k}}),a.register("list.js/src/sort.js",function(a,b,c){var d=b("natural-sort"),e=b("classes"),f=b("events"),g=b("get-by-class"),h=b("get-attribute");c.exports=function(a){a.sortFunction=a.sortFunction||function(a,b,c){return c.desc="desc"==c.order?!0:!1,d(a.values()[c.valueName],b.values()[c.valueName],c)};var b={els:void 0,clear:function(){for(var a=0,c=b.els.length;c>a;a++)e(b.els[a]).remove("asc"),e(b.els[a]).remove("desc")},getOrder:function(a){var b=h(a,"data-order");return"asc"==b||"desc"==b?b:e(a).has("desc")?"asc":e(a).has("asc")?"desc":"asc"},getInSensitive:function(a,b){var c=h(a,"data-insensitive");b.insensitive="true"===c?!0:!1},setOrder:function(a){for(var c=0,d=b.els.length;d>c;c++){var f=b.els[c];if(h(f,"data-sort")===a.valueName){var g=h(f,"data-order");"asc"==g||"desc"==g?g==a.order&&e(f).add(a.order):e(f).add(a.order)}}}},c=function(){a.trigger("sortStart"),options={};var c=arguments[0].currentTarget||arguments[0].srcElement||void 0;c?(options.valueName=h(c,"data-sort"),b.getInSensitive(c,options),options.order=b.getOrder(c)):(options=arguments[1]||options,options.valueName=arguments[0],options.order=options.order||"asc",options.insensitive="undefined"==typeof options.insensitive?!0:options.insensitive),b.clear(),b.setOrder(options),options.sortFunction=options.sortFunction||a.sortFunction,a.items.sort(function(a,b){return options.sortFunction(a,b,options)}),a.update(),a.trigger("sortComplete")};return a.handlers.sortStart=a.handlers.sortStart||[],a.handlers.sortComplete=a.handlers.sortComplete||[],b.els=g(a.listContainer,a.sortClass),f.bind(b.els,"click",c),a.on("searchStart",b.clear),a.on("filterStart",b.clear),a.helpers.classes=e,a.helpers.naturalSort=d,a.helpers.events=f,a.helpers.getAttribute=h,c}}),a.register("list.js/src/item.js",function(a,b,c){c.exports=function(a){return function(b,c,d){var e=this;this._values={},this.found=!1,this.filtered=!1;var f=function(b,c,d){if(void 0===c)d?e.values(b,d):e.values(b);else{e.elm=c;var f=a.templater.get(e,b);e.values(f)}};this.values=function(b,c){if(void 0===b)return e._values;for(var d in b)e._values[d]=b[d];c!==!0&&a.templater.set(e,e.values())},this.show=function(){a.templater.show(e)},this.hide=function(){a.templater.hide(e)},this.matching=function(){return a.filtered&&a.searched&&e.found&&e.filtered||a.filtered&&!a.searched&&e.filtered||!a.filtered&&a.searched&&e.found||!a.filtered&&!a.searched},this.visible=function(){return e.elm.parentNode==a.list?!0:!1},f(b,c,d)}}}),a.register("list.js/src/templater.js",function(a,b,c){var d=b("get-by-class"),e=function(a){function b(b){if(void 0===b){for(var c=a.list.childNodes,d=0,e=c.length;e>d;d++)if(void 0===c[d].data)return c[d];return null}if(-1!==b.indexOf("<")){var f=document.createElement("div");return f.innerHTML=b,f.firstChild}return document.getElementById(a.item)}var c=b(a.item),e=this;this.get=function(a,b){e.create(a);for(var c={},f=0,g=b.length;g>f;f++){var h=d(a.elm,b[f],!0);c[b[f]]=h?h.innerHTML:""}return c},this.set=function(a,b){if(!e.create(a))for(var c in b)if(b.hasOwnProperty(c)){var f=d(a.elm,c,!0);f&&("IMG"===f.tagName&&""!==b[c]?f.src=b[c]:f.innerHTML=b[c])}},this.create=function(a){if(void 0!==a.elm)return!1;var b=c.cloneNode(!0);return b.removeAttribute("id"),a.elm=b,e.set(a,a.values()),!0},this.remove=function(b){a.list.removeChild(b.elm)},this.show=function(b){e.create(b),a.list.appendChild(b.elm)},this.hide=function(b){void 0!==b.elm&&b.elm.parentNode===a.list&&a.list.removeChild(b.elm)},this.clear=function(){if(a.list.hasChildNodes())for(;a.list.childNodes.length>=1;)a.list.removeChild(a.list.firstChild)}};c.exports=function(a){return new e(a)}}),a.register("list.js/src/filter.js",function(a,b,c){c.exports=function(a){return a.handlers.filterStart=a.handlers.filterStart||[],a.handlers.filterComplete=a.handlers.filterComplete||[],function(b){if(a.trigger("filterStart"),a.i=1,a.reset.filter(),void 0===b)a.filtered=!1;else{a.filtered=!0;for(var c=a.items,d=0,e=c.length;e>d;d++){var f=c[d];f.filtered=b(f)?!0:!1}}return a.update(),a.trigger("filterComplete"),a.visibleItems}}}),a.register("list.js/src/add-async.js",function(a,b,c){c.exports=function(a){return function(b,c,d){var e=b.splice(0,100);d=d||[],d=d.concat(a.add(e)),b.length>0?setTimeout(function(){addAsync(b,c,d)},10):(a.update(),c(d))}}}),a.register("list.js/src/parse.js",function(a,b,c){c.exports=function(a){var c=b("./item")(a),d=function(a){for(var b=a.childNodes,c=[],d=0,e=b.length;e>d;d++)void 0===b[d].data&&c.push(b[d]);return c},e=function(b,d){for(var e=0,f=b.length;f>e;e++)a.items.push(new c(d,b[e]))},f=function(b,c){var d=b.splice(0,100);e(d,c),b.length>0?setTimeout(function(){init.items.indexAsync(b,c)},10):a.update()};return function(){var b=d(a.list),c=a.valueNames;a.indexAsync?f(b,c):e(b,c)}}}),a.alias("component-classes/index.js","list.js/deps/classes/index.js"),a.alias("component-classes/index.js","classes/index.js"),a.alias("component-indexof/index.js","component-classes/deps/indexof/index.js"),a.alias("segmentio-extend/index.js","list.js/deps/extend/index.js"),a.alias("segmentio-extend/index.js","extend/index.js"),a.alias("component-indexof/index.js","list.js/deps/indexof/index.js"),a.alias("component-indexof/index.js","indexof/index.js"),a.alias("javve-events/index.js","list.js/deps/events/index.js"),a.alias("javve-events/index.js","events/index.js"),a.alias("component-event/index.js","javve-events/deps/event/index.js"),a.alias("timoxley-to-array/index.js","javve-events/deps/to-array/index.js"),a.alias("javve-get-by-class/index.js","list.js/deps/get-by-class/index.js"),a.alias("javve-get-by-class/index.js","get-by-class/index.js"),a.alias("javve-get-attribute/index.js","list.js/deps/get-attribute/index.js"),a.alias("javve-get-attribute/index.js","get-attribute/index.js"),a.alias("javve-natural-sort/index.js","list.js/deps/natural-sort/index.js"),a.alias("javve-natural-sort/index.js","natural-sort/index.js"),a.alias("javve-to-string/index.js","list.js/deps/to-string/index.js"),a.alias("javve-to-string/index.js","list.js/deps/to-string/index.js"),a.alias("javve-to-string/index.js","to-string/index.js"),a.alias("javve-to-string/index.js","javve-to-string/index.js"),a.alias("component-type/index.js","list.js/deps/type/index.js"),a.alias("component-type/index.js","type/index.js"),"object"==typeof exports?module.exports=a("list.js"):"function"==typeof define&&define.amd?define(function(){return a("list.js")}):this.List=a("list.js")}();

//List.js fuzzy search plugin import
!function(){function a(b,c,d){var e=a.resolve(b);if(null==e){d=d||b,c=c||"root";var f=new Error('Failed to require "'+d+'" from "'+c+'"');throw f.path=d,f.parent=c,f.require=!0,f}var g=a.modules[e];if(!g._resolving&&!g.exports){var h={};h.exports={},h.client=h.component=!0,g._resolving=!0,g.call(this,h.exports,a.relative(e),h),delete g._resolving,g.exports=h.exports}return g.exports}a.modules={},a.aliases={},a.resolve=function(b){"/"===b.charAt(0)&&(b=b.slice(1));for(var c=[b,b+".js",b+".json",b+"/index.js",b+"/index.json"],d=0;d<c.length;d++){var b=c[d];if(a.modules.hasOwnProperty(b))return b;if(a.aliases.hasOwnProperty(b))return a.aliases[b]}},a.normalize=function(a,b){var c=[];if("."!=b.charAt(0))return b;a=a.split("/"),b=b.split("/");for(var d=0;d<b.length;++d)".."==b[d]?a.pop():"."!=b[d]&&""!=b[d]&&c.push(b[d]);return a.concat(c).join("/")},a.register=function(b,c){a.modules[b]=c},a.alias=function(b,c){if(!a.modules.hasOwnProperty(b))throw new Error('Failed to alias "'+b+'", it does not exist');a.aliases[c]=b},a.relative=function(b){function c(a,b){for(var c=a.length;c--;)if(a[c]===b)return c;return-1}function d(c){var e=d.resolve(c);return a(e,b,c)}var e=a.normalize(b,"..");return d.resolve=function(d){var f=d.charAt(0);if("/"==f)return d.slice(1);if("."==f)return a.normalize(e,d);var g=b.split("/"),h=c(g,"deps")+1;return h||(h=0),d=g.slice(0,h+1).join("/")+"/deps/"+d},d.exists=function(b){return a.modules.hasOwnProperty(d.resolve(b))},d},a.register("component-indexof/index.js",function(a,b,c){c.exports=function(a,b){if(a.indexOf)return a.indexOf(b);for(var c=0;c<a.length;++c)if(a[c]===b)return c;return-1}}),a.register("component-classes/index.js",function(a,b,c){function d(a){if(!a)throw new Error("A DOM element reference is required");this.el=a,this.list=a.classList}var e=b("indexof"),f=/\s+/,g=Object.prototype.toString;c.exports=function(a){return new d(a)},d.prototype.add=function(a){if(this.list)return this.list.add(a),this;var b=this.array(),c=e(b,a);return~c||b.push(a),this.el.className=b.join(" "),this},d.prototype.remove=function(a){if("[object RegExp]"==g.call(a))return this.removeMatching(a);if(this.list)return this.list.remove(a),this;var b=this.array(),c=e(b,a);return~c&&b.splice(c,1),this.el.className=b.join(" "),this},d.prototype.removeMatching=function(a){for(var b=this.array(),c=0;c<b.length;c++)a.test(b[c])&&this.remove(b[c]);return this},d.prototype.toggle=function(a){return this.list?(this.list.toggle(a),this):(this.has(a)?this.remove(a):this.add(a),this)},d.prototype.array=function(){var a=this.el.className.replace(/^\s+|\s+$/g,""),b=a.split(f);return""===b[0]&&b.shift(),b},d.prototype.has=d.prototype.contains=function(a){return this.list?this.list.contains(a):!!~e(this.array(),a)}}),a.register("segmentio-extend/index.js",function(a,b,c){c.exports=function(a){for(var b,c=Array.prototype.slice.call(arguments,1),d=0;b=c[d];d++)if(b)for(var e in b)a[e]=b[e];return a}}),a.register("component-event/index.js",function(a){var b=void 0!==window.addEventListener?"addEventListener":"attachEvent",c=void 0!==window.removeEventListener?"removeEventListener":"detachEvent",d="addEventListener"!==b?"on":"";a.bind=function(a,c,e,f){return a[b](d+c,e,f||!1),e},a.unbind=function(a,b,e,f){return a[c](d+b,e,f||!1),e}}),a.register("component-type/index.js",function(a,b,c){var d=Object.prototype.toString;c.exports=function(a){switch(d.call(a)){case"[object Function]":return"function";case"[object Date]":return"date";case"[object RegExp]":return"regexp";case"[object Arguments]":return"arguments";case"[object Array]":return"array";case"[object String]":return"string"}return null===a?"null":void 0===a?"undefined":a&&1===a.nodeType?"element":a===Object(a)?"object":typeof a}}),a.register("timoxley-is-collection/index.js",function(a,b,c){function d(a){return"object"==typeof a&&/^\[object (NodeList)\]$/.test(Object.prototype.toString.call(a))&&a.hasOwnProperty("length")&&(0==a.length||"object"==typeof a[0]&&a[0].nodeType>0)}var e=b("type");c.exports=function(a){var b=e(a);if("array"===b)return 1;switch(b){case"arguments":return 2;case"object":if(d(a))return 2;try{if("length"in a&&!a.tagName&&(!a.scrollTo||!a.document)&&!a.apply)return 2}catch(c){}default:return 0}}}),a.register("javve-events/index.js",function(a,b){var c=b("event"),d=b("is-collection");a.bind=function(a,b,e,f){if(d(a)){if(a&&void 0!==a[0])for(var g=0;g<a.length;g++)c.bind(a[g],b,e,f)}else c.bind(a,b,e,f)},a.unbind=function(a,b,e,f){if(d(a)){if(a&&void 0!==a[0])for(var g=0;g<a.length;g++)c.unbind(a[g],b,e,f)}else c.unbind(a,b,e,f)}}),a.register("javve-get-by-class/index.js",function(a,b,c){c.exports=function(){return document.getElementsByClassName?function(a,b,c){return c?a.getElementsByClassName(b)[0]:a.getElementsByClassName(b)}:document.querySelector?function(a,b,c){return c?a.querySelector(b):a.querySelectorAll(b)}:function(a,b,c){var d=[],e="*";null==a&&(a=document);for(var f=a.getElementsByTagName(e),g=f.length,h=new RegExp("(^|\\s)"+b+"(\\s|$)"),i=0,j=0;g>i;i++)if(h.test(f[i].className)){if(c)return f[i];d[j]=f[i],j++}return d}}()}),a.register("javve-to-string/index.js",function(a,b,c){c.exports=function(a){return a=void 0===a?"":a,a=null===a?"":a,a=a.toString()}}),a.register("list.fuzzysearch.js/index.js",function(a,b,c){var d=(b("classes"),b("events")),e=b("extend"),f=b("to-string"),g=b("get-by-class");c.exports=function(a){a=a||{},e(a,{location:0,distance:100,threshold:.4,multiSearch:!0,searchClass:"fuzzy-search"});var c,h=b("./src/fuzzy"),i={search:function(b,d){for(var e=a.multiSearch?b.replace(/ +$/,"").split(/ +/):[b],f=0,g=c.items.length;g>f;f++)i.item(c.items[f],d,e)},item:function(a,b,c){for(var d=!0,e=0;e<c.length;e++){for(var f=!1,g=0,h=b.length;h>g;g++)i.values(a.values(),b[g],c[e])&&(f=!0);f||(d=!1)}a.found=d},values:function(b,c,d){if(b.hasOwnProperty(c)){var e=f(b[c]).toLowerCase();if(h(e,d,a))return!0}return!1}};return{init:function(b){c=b,d.bind(g(c.listContainer,a.searchClass),"keyup",function(a){var b=a.target||a.srcElement;c.search(b.value,i.search)})},search:function(a,b){c.search(a,b,i.search)},name:a.name||"fuzzySearch"}}}),a.register("list.fuzzysearch.js/src/fuzzy.js",function(a,b,c){c.exports=function(a,b,c){function d(a,c){var d=a/b.length,e=Math.abs(h-c);return f?d+e/f:e?1:d}var e=c.location||0,f=c.distance||100,g=c.threshold||.4;if(b===a)return!0;if(b.length>32)return!1;var h=e,i=function(){var a,c={};for(a=0;a<b.length;a++)c[b.charAt(a)]=0;for(a=0;a<b.length;a++)c[b.charAt(a)]|=1<<b.length-a-1;return c}(),j=g,k=a.indexOf(b,h);-1!=k&&(j=Math.min(d(0,k),j),k=a.lastIndexOf(b,h+b.length),-1!=k&&(j=Math.min(d(0,k),j)));var l=1<<b.length-1;k=-1;for(var m,n,o,p=b.length+a.length,q=0;q<b.length;q++){for(m=0,n=p;n>m;)d(q,h+n)<=j?m=n:p=n,n=Math.floor((p-m)/2+m);p=n;var r=Math.max(1,h-n+1),s=Math.min(h+n,a.length)+b.length,t=Array(s+2);t[s+1]=(1<<q)-1;for(var u=s;u>=r;u--){var v=i[a.charAt(u-1)];if(t[u]=0===q?(t[u+1]<<1|1)&v:(t[u+1]<<1|1)&v|((o[u+1]|o[u])<<1|1)|o[u+1],t[u]&l){var w=d(q,u-1);if(j>=w){if(j=w,k=u-1,!(k>h))break;r=Math.max(1,2*h-k)}}}if(d(q+1,h)>j)break;o=t}return 0>k?!1:!0}}),a.alias("component-classes/index.js","list.fuzzysearch.js/deps/classes/index.js"),a.alias("component-classes/index.js","classes/index.js"),a.alias("component-indexof/index.js","component-classes/deps/indexof/index.js"),a.alias("segmentio-extend/index.js","list.fuzzysearch.js/deps/extend/index.js"),a.alias("segmentio-extend/index.js","extend/index.js"),a.alias("javve-events/index.js","list.fuzzysearch.js/deps/events/index.js"),a.alias("javve-events/index.js","events/index.js"),a.alias("component-event/index.js","javve-events/deps/event/index.js"),a.alias("timoxley-is-collection/index.js","javve-events/deps/is-collection/index.js"),a.alias("component-type/index.js","timoxley-is-collection/deps/type/index.js"),a.alias("javve-get-by-class/index.js","list.fuzzysearch.js/deps/get-by-class/index.js"),a.alias("javve-get-by-class/index.js","get-by-class/index.js"),a.alias("javve-to-string/index.js","list.fuzzysearch.js/deps/to-string/index.js"),a.alias("javve-to-string/index.js","list.fuzzysearch.js/deps/to-string/index.js"),a.alias("javve-to-string/index.js","to-string/index.js"),a.alias("javve-to-string/index.js","javve-to-string/index.js"),a.alias("list.fuzzysearch.js/index.js","list.fuzzysearch.js/index.js"),"object"==typeof exports?module.exports=a("list.fuzzysearch.js"):"function"==typeof define&&define.amd?define(function(){return a("list.fuzzysearch.js")}):this.ListFuzzySearch=a("list.fuzzysearch.js")}();

//Bootstrap Datepicker
!function(t,e){function i(){return new Date(Date.UTC.apply(Date,arguments))}function a(){var t=new Date;return i(t.getFullYear(),t.getMonth(),t.getDate())}function s(t){return function(){return this[t].apply(this,arguments)}}function n(e,i){function a(t,e){return e.toLowerCase()}var s,n=t(e).data(),r={},o=new RegExp("^"+i.toLowerCase()+"([A-Z])");i=new RegExp("^"+i.toLowerCase());for(var h in n)i.test(h)&&(s=h.replace(o,a),r[s]=n[h]);return r}function r(e){var i={};if(f[e]||(e=e.split("-")[0],f[e])){var a=f[e];return t.each(p,function(t,e){e in a&&(i[e]=a[e])}),i}}var o=t(window),h=function(){var e={get:function(t){return this.slice(t)[0]},contains:function(t){for(var e=t&&t.valueOf(),i=0,a=this.length;a>i;i++)if(this[i].valueOf()===e)return i;return-1},remove:function(t){this.splice(t,1)},replace:function(e){e&&(t.isArray(e)||(e=[e]),this.clear(),this.push.apply(this,e))},clear:function(){this.splice(0)},copy:function(){var t=new h;return t.replace(this),t}};return function(){var i=[];return i.push.apply(i,arguments),t.extend(i,e),i}}(),d=function(e,i){this.dates=new h,this.viewDate=a(),this.focusDate=null,this._process_options(i),this.element=t(e),this.isInline=!1,this.isInput=this.element.is("input"),this.component=this.element.is(".date")?this.element.find(".add-on, .input-group-addon, .btn"):!1,this.hasInput=this.component&&this.element.find("input").length,this.component&&0===this.component.length&&(this.component=!1),this.picker=t(g.template),this._buildEvents(),this._attachEvents(),this.isInline?this.picker.addClass("datepicker-inline").appendTo(this.element):this.picker.addClass("datepicker-dropdown dropdown-menu"),this.o.rtl&&this.picker.addClass("datepicker-rtl"),this.viewMode=this.o.startView,this.o.calendarWeeks&&this.picker.find("tfoot th.today").attr("colspan",function(t,e){return parseInt(e)+1}),this._allow_update=!1,this.setStartDate(this._o.startDate),this.setEndDate(this._o.endDate),this.setDaysOfWeekDisabled(this.o.daysOfWeekDisabled),this.fillDow(),this.fillMonths(),this._allow_update=!0,this.update(),this.showMode(),this.isInline&&this.show()};d.prototype={constructor:d,_process_options:function(e){this._o=t.extend({},this._o,e);var i=this.o=t.extend({},this._o),a=i.language;switch(f[a]||(a=a.split("-")[0],f[a]||(a=u.language)),i.language=a,i.startView){case 2:case"decade":i.startView=2;break;case 1:case"year":i.startView=1;break;default:i.startView=0}switch(i.minViewMode){case 1:case"months":i.minViewMode=1;break;case 2:case"years":i.minViewMode=2;break;default:i.minViewMode=0}i.startView=Math.max(i.startView,i.minViewMode),i.multidate!==!0&&(i.multidate=Number(i.multidate)||!1,i.multidate=i.multidate!==!1?Math.max(0,i.multidate):1),i.multidateSeparator=String(i.multidateSeparator),i.weekStart%=7,i.weekEnd=(i.weekStart+6)%7;var s=g.parseFormat(i.format);i.startDate!==-1/0&&(i.startDate=i.startDate?i.startDate instanceof Date?this._local_to_utc(this._zero_time(i.startDate)):g.parseDate(i.startDate,s,i.language):-1/0),1/0!==i.endDate&&(i.endDate=i.endDate?i.endDate instanceof Date?this._local_to_utc(this._zero_time(i.endDate)):g.parseDate(i.endDate,s,i.language):1/0),i.daysOfWeekDisabled=i.daysOfWeekDisabled||[],t.isArray(i.daysOfWeekDisabled)||(i.daysOfWeekDisabled=i.daysOfWeekDisabled.split(/[,\s]*/)),i.daysOfWeekDisabled=t.map(i.daysOfWeekDisabled,function(t){return parseInt(t,10)});var n=String(i.orientation).toLowerCase().split(/\s+/g),r=i.orientation.toLowerCase();if(n=t.grep(n,function(t){return/^auto|left|right|top|bottom$/.test(t)}),i.orientation={x:"auto",y:"auto"},r&&"auto"!==r)if(1===n.length)switch(n[0]){case"top":case"bottom":i.orientation.y=n[0];break;case"left":case"right":i.orientation.x=n[0]}else r=t.grep(n,function(t){return/^left|right$/.test(t)}),i.orientation.x=r[0]||"auto",r=t.grep(n,function(t){return/^top|bottom$/.test(t)}),i.orientation.y=r[0]||"auto";else;},_events:[],_secondaryEvents:[],_applyEvents:function(t){for(var i,a,s,n=0;n<t.length;n++)i=t[n][0],2===t[n].length?(a=e,s=t[n][1]):3===t[n].length&&(a=t[n][1],s=t[n][2]),i.on(s,a)},_unapplyEvents:function(t){for(var i,a,s,n=0;n<t.length;n++)i=t[n][0],2===t[n].length?(s=e,a=t[n][1]):3===t[n].length&&(s=t[n][1],a=t[n][2]),i.off(a,s)},_buildEvents:function(){this.isInput?this._events=[[this.element,{focus:t.proxy(this.show,this),keyup:t.proxy(function(e){-1===t.inArray(e.keyCode,[27,37,39,38,40,32,13,9])&&this.update()},this),keydown:t.proxy(this.keydown,this)}]]:this.component&&this.hasInput?this._events=[[this.element.find("input"),{focus:t.proxy(this.show,this),keyup:t.proxy(function(e){-1===t.inArray(e.keyCode,[27,37,39,38,40,32,13,9])&&this.update()},this),keydown:t.proxy(this.keydown,this)}],[this.component,{click:t.proxy(this.show,this)}]]:this.element.is("div")?this.isInline=!0:this._events=[[this.element,{click:t.proxy(this.show,this)}]],this._events.push([this.element,"*",{blur:t.proxy(function(t){this._focused_from=t.target},this)}],[this.element,{blur:t.proxy(function(t){this._focused_from=t.target},this)}]),this._secondaryEvents=[[this.picker,{click:t.proxy(this.click,this)}],[t(window),{resize:t.proxy(this.place,this)}],[t(document),{"mousedown touchstart":t.proxy(function(t){this.element.is(t.target)||this.element.find(t.target).length||this.picker.is(t.target)||this.picker.find(t.target).length||this.hide()},this)}]]},_attachEvents:function(){this._detachEvents(),this._applyEvents(this._events)},_detachEvents:function(){this._unapplyEvents(this._events)},_attachSecondaryEvents:function(){this._detachSecondaryEvents(),this._applyEvents(this._secondaryEvents)},_detachSecondaryEvents:function(){this._unapplyEvents(this._secondaryEvents)},_trigger:function(e,i){var a=i||this.dates.get(-1),s=this._utc_to_local(a);this.element.trigger({type:e,date:s,dates:t.map(this.dates,this._utc_to_local),format:t.proxy(function(t,e){0===arguments.length?(t=this.dates.length-1,e=this.o.format):"string"==typeof t&&(e=t,t=this.dates.length-1),e=e||this.o.format;var i=this.dates.get(t);return g.formatDate(i,e,this.o.language)},this)})},show:function(){this.isInline||this.picker.appendTo("body"),this.picker.show(),this.place(),this._attachSecondaryEvents(),this._trigger("show")},hide:function(){this.isInline||this.picker.is(":visible")&&(this.focusDate=null,this.picker.hide().detach(),this._detachSecondaryEvents(),this.viewMode=this.o.startView,this.showMode(),this.o.forceParse&&(this.isInput&&this.element.val()||this.hasInput&&this.element.find("input").val())&&this.setValue(),this._trigger("hide"))},remove:function(){this.hide(),this._detachEvents(),this._detachSecondaryEvents(),this.picker.remove(),delete this.element.data().datepicker,this.isInput||delete this.element.data().date},_utc_to_local:function(t){return t&&new Date(t.getTime()+6e4*t.getTimezoneOffset())},_local_to_utc:function(t){return t&&new Date(t.getTime()-6e4*t.getTimezoneOffset())},_zero_time:function(t){return t&&new Date(t.getFullYear(),t.getMonth(),t.getDate())},_zero_utc_time:function(t){return t&&new Date(Date.UTC(t.getUTCFullYear(),t.getUTCMonth(),t.getUTCDate()))},getDates:function(){return t.map(this.dates,this._utc_to_local)},getUTCDates:function(){return t.map(this.dates,function(t){return new Date(t)})},getDate:function(){return this._utc_to_local(this.getUTCDate())},getUTCDate:function(){return new Date(this.dates.get(-1))},setDates:function(){var e=t.isArray(arguments[0])?arguments[0]:arguments;this.update.apply(this,e),this._trigger("changeDate"),this.setValue()},setUTCDates:function(){var e=t.isArray(arguments[0])?arguments[0]:arguments;this.update.apply(this,t.map(e,this._utc_to_local)),this._trigger("changeDate"),this.setValue()},setDate:s("setDates"),setUTCDate:s("setUTCDates"),setValue:function(){var t=this.getFormattedDate();this.isInput?this.element.val(t).change():this.component&&this.element.find("input").val(t).change()},getFormattedDate:function(i){i===e&&(i=this.o.format);var a=this.o.language;return t.map(this.dates,function(t){return g.formatDate(t,i,a)}).join(this.o.multidateSeparator)},setStartDate:function(t){this._process_options({startDate:t}),this.update(),this.updateNavArrows()},setEndDate:function(t){this._process_options({endDate:t}),this.update(),this.updateNavArrows()},setDaysOfWeekDisabled:function(t){this._process_options({daysOfWeekDisabled:t}),this.update(),this.updateNavArrows()},place:function(){if(!this.isInline){var e=this.picker.outerWidth(),i=this.picker.outerHeight(),a=10,s=o.width(),n=o.height(),r=o.scrollTop(),h=parseInt(this.element.parents().filter(function(){return"auto"!==t(this).css("z-index")}).first().css("z-index"))+10,d=this.component?this.component.parent().offset():this.element.offset(),l=this.component?this.component.outerHeight(!0):this.element.outerHeight(!1),c=this.component?this.component.outerWidth(!0):this.element.outerWidth(!1),u=d.left,p=d.top;this.picker.removeClass("datepicker-orient-top datepicker-orient-bottom datepicker-orient-right datepicker-orient-left"),"auto"!==this.o.orientation.x?(this.picker.addClass("datepicker-orient-"+this.o.orientation.x),"right"===this.o.orientation.x&&(u-=e-c)):(this.picker.addClass("datepicker-orient-left"),d.left<0?u-=d.left-a:d.left+e>s&&(u=s-e-a));var f,g,D=this.o.orientation.y;"auto"===D&&(f=-r+d.top-i,g=r+n-(d.top+l+i),D=Math.max(f,g)===g?"top":"bottom"),this.picker.addClass("datepicker-orient-"+D),"top"===D?p+=l:p-=i+parseInt(this.picker.css("padding-top")),this.picker.css({top:p,left:u,zIndex:h})}},_allow_update:!0,update:function(){if(this._allow_update){var e=this.dates.copy(),i=[],a=!1;arguments.length?(t.each(arguments,t.proxy(function(t,e){e instanceof Date&&(e=this._local_to_utc(e)),i.push(e)},this)),a=!0):(i=this.isInput?this.element.val():this.element.data("date")||this.element.find("input").val(),i=i&&this.o.multidate?i.split(this.o.multidateSeparator):[i],delete this.element.data().date),i=t.map(i,t.proxy(function(t){return g.parseDate(t,this.o.format,this.o.language)},this)),i=t.grep(i,t.proxy(function(t){return t<this.o.startDate||t>this.o.endDate||!t},this),!0),this.dates.replace(i),this.dates.length?this.viewDate=new Date(this.dates.get(-1)):this.viewDate<this.o.startDate?this.viewDate=new Date(this.o.startDate):this.viewDate>this.o.endDate&&(this.viewDate=new Date(this.o.endDate)),a?this.setValue():i.length&&String(e)!==String(this.dates)&&this._trigger("changeDate"),!this.dates.length&&e.length&&this._trigger("clearDate"),this.fill()}},fillDow:function(){var t=this.o.weekStart,e="<tr>";if(this.o.calendarWeeks){var i='<th class="cw">&nbsp;</th>';e+=i,this.picker.find(".datepicker-days thead tr:first-child").prepend(i)}for(;t<this.o.weekStart+7;)e+='<th class="dow">'+f[this.o.language].daysMin[t++%7]+"</th>";e+="</tr>",this.picker.find(".datepicker-days thead").append(e)},fillMonths:function(){for(var t="",e=0;12>e;)t+='<span class="month">'+f[this.o.language].monthsShort[e++]+"</span>";this.picker.find(".datepicker-months td").html(t)},setRange:function(e){e&&e.length?this.range=t.map(e,function(t){return t.valueOf()}):delete this.range,this.fill()},getClassNames:function(e){var i=[],a=this.viewDate.getUTCFullYear(),s=this.viewDate.getUTCMonth(),n=new Date;return e.getUTCFullYear()<a||e.getUTCFullYear()===a&&e.getUTCMonth()<s?i.push("old"):(e.getUTCFullYear()>a||e.getUTCFullYear()===a&&e.getUTCMonth()>s)&&i.push("new"),this.focusDate&&e.valueOf()===this.focusDate.valueOf()&&i.push("focused"),this.o.todayHighlight&&e.getUTCFullYear()===n.getFullYear()&&e.getUTCMonth()===n.getMonth()&&e.getUTCDate()===n.getDate()&&i.push("today"),-1!==this.dates.contains(e)&&i.push("active"),(e.valueOf()<this.o.startDate||e.valueOf()>this.o.endDate||-1!==t.inArray(e.getUTCDay(),this.o.daysOfWeekDisabled))&&i.push("disabled"),this.range&&(e>this.range[0]&&e<this.range[this.range.length-1]&&i.push("range"),-1!==t.inArray(e.valueOf(),this.range)&&i.push("selected")),i},fill:function(){var a,s=new Date(this.viewDate),n=s.getUTCFullYear(),r=s.getUTCMonth(),o=this.o.startDate!==-1/0?this.o.startDate.getUTCFullYear():-1/0,h=this.o.startDate!==-1/0?this.o.startDate.getUTCMonth():-1/0,d=1/0!==this.o.endDate?this.o.endDate.getUTCFullYear():1/0,l=1/0!==this.o.endDate?this.o.endDate.getUTCMonth():1/0,c=f[this.o.language].today||f.en.today||"",u=f[this.o.language].clear||f.en.clear||"";this.picker.find(".datepicker-days thead th.datepicker-switch").text(f[this.o.language].months[r]+" "+n),this.picker.find("tfoot th.today").text(c).toggle(this.o.todayBtn!==!1),this.picker.find("tfoot th.clear").text(u).toggle(this.o.clearBtn!==!1),this.updateNavArrows(),this.fillMonths();var p=i(n,r-1,28),D=g.getDaysInMonth(p.getUTCFullYear(),p.getUTCMonth());p.setUTCDate(D),p.setUTCDate(D-(p.getUTCDay()-this.o.weekStart+7)%7);var m=new Date(p);m.setUTCDate(m.getUTCDate()+42),m=m.valueOf();for(var v,y=[];p.valueOf()<m;){if(p.getUTCDay()===this.o.weekStart&&(y.push("<tr>"),this.o.calendarWeeks)){var w=new Date(+p+(this.o.weekStart-p.getUTCDay()-7)%7*864e5),k=new Date(Number(w)+(11-w.getUTCDay())%7*864e5),C=new Date(Number(C=i(k.getUTCFullYear(),0,1))+(11-C.getUTCDay())%7*864e5),_=(k-C)/864e5/7+1;y.push('<td class="cw">'+_+"</td>")}if(v=this.getClassNames(p),v.push("day"),this.o.beforeShowDay!==t.noop){var T=this.o.beforeShowDay(this._utc_to_local(p));T===e?T={}:"boolean"==typeof T?T={enabled:T}:"string"==typeof T&&(T={classes:T}),T.enabled===!1&&v.push("disabled"),T.classes&&(v=v.concat(T.classes.split(/\s+/))),T.tooltip&&(a=T.tooltip)}v=t.unique(v),y.push('<td class="'+v.join(" ")+'"'+(a?' title="'+a+'"':"")+">"+p.getUTCDate()+"</td>"),p.getUTCDay()===this.o.weekEnd&&y.push("</tr>"),p.setUTCDate(p.getUTCDate()+1)}this.picker.find(".datepicker-days tbody").empty().append(y.join(""));var b=this.picker.find(".datepicker-months").find("th:eq(1)").text(n).end().find("span").removeClass("active");t.each(this.dates,function(t,e){e.getUTCFullYear()===n&&b.eq(e.getUTCMonth()).addClass("active")}),(o>n||n>d)&&b.addClass("disabled"),n===o&&b.slice(0,h).addClass("disabled"),n===d&&b.slice(l+1).addClass("disabled"),y="",n=10*parseInt(n/10,10);var M=this.picker.find(".datepicker-years").find("th:eq(1)").text(n+"-"+(n+9)).end().find("td");n-=1;for(var U,S=t.map(this.dates,function(t){return t.getUTCFullYear()}),x=-1;11>x;x++)U=["year"],-1===x?U.push("old"):10===x&&U.push("new"),-1!==t.inArray(n,S)&&U.push("active"),(o>n||n>d)&&U.push("disabled"),y+='<span class="'+U.join(" ")+'">'+n+"</span>",n+=1;M.html(y)},updateNavArrows:function(){if(this._allow_update){var t=new Date(this.viewDate),e=t.getUTCFullYear(),i=t.getUTCMonth();switch(this.viewMode){case 0:this.picker.find(".prev").css(this.o.startDate!==-1/0&&e<=this.o.startDate.getUTCFullYear()&&i<=this.o.startDate.getUTCMonth()?{visibility:"hidden"}:{visibility:"visible"}),this.picker.find(".next").css(1/0!==this.o.endDate&&e>=this.o.endDate.getUTCFullYear()&&i>=this.o.endDate.getUTCMonth()?{visibility:"hidden"}:{visibility:"visible"});break;case 1:case 2:this.picker.find(".prev").css(this.o.startDate!==-1/0&&e<=this.o.startDate.getUTCFullYear()?{visibility:"hidden"}:{visibility:"visible"}),this.picker.find(".next").css(1/0!==this.o.endDate&&e>=this.o.endDate.getUTCFullYear()?{visibility:"hidden"}:{visibility:"visible"})}}},click:function(e){e.preventDefault();var a,s,n,r=t(e.target).closest("span, td, th");if(1===r.length)switch(r[0].nodeName.toLowerCase()){case"th":switch(r[0].className){case"datepicker-switch":this.showMode(1);break;case"prev":case"next":var o=g.modes[this.viewMode].navStep*("prev"===r[0].className?-1:1);switch(this.viewMode){case 0:this.viewDate=this.moveMonth(this.viewDate,o),this._trigger("changeMonth",this.viewDate);break;case 1:case 2:this.viewDate=this.moveYear(this.viewDate,o),1===this.viewMode&&this._trigger("changeYear",this.viewDate)}this.fill();break;case"today":var h=new Date;h=i(h.getFullYear(),h.getMonth(),h.getDate(),0,0,0),this.showMode(-2);var d="linked"===this.o.todayBtn?null:"view";this._setDate(h,d);break;case"clear":var l;this.isInput?l=this.element:this.component&&(l=this.element.find("input")),l&&l.val("").change(),this.update(),this._trigger("changeDate"),this.o.autoclose&&this.hide()}break;case"span":r.is(".disabled")||(this.viewDate.setUTCDate(1),r.is(".month")?(n=1,s=r.parent().find("span").index(r),a=this.viewDate.getUTCFullYear(),this.viewDate.setUTCMonth(s),this._trigger("changeMonth",this.viewDate),1===this.o.minViewMode&&this._setDate(i(a,s,n))):(n=1,s=0,a=parseInt(r.text(),10)||0,this.viewDate.setUTCFullYear(a),this._trigger("changeYear",this.viewDate),2===this.o.minViewMode&&this._setDate(i(a,s,n))),this.showMode(-1),this.fill());break;case"td":r.is(".day")&&!r.is(".disabled")&&(n=parseInt(r.text(),10)||1,a=this.viewDate.getUTCFullYear(),s=this.viewDate.getUTCMonth(),r.is(".old")?0===s?(s=11,a-=1):s-=1:r.is(".new")&&(11===s?(s=0,a+=1):s+=1),this._setDate(i(a,s,n)))}this.picker.is(":visible")&&this._focused_from&&t(this._focused_from).focus(),delete this._focused_from},_toggle_multidate:function(t){var e=this.dates.contains(t);if(t?-1!==e?this.dates.remove(e):this.dates.push(t):this.dates.clear(),"number"==typeof this.o.multidate)for(;this.dates.length>this.o.multidate;)this.dates.remove(0)},_setDate:function(t,e){e&&"date"!==e||this._toggle_multidate(t&&new Date(t)),e&&"view"!==e||(this.viewDate=t&&new Date(t)),this.fill(),this.setValue(),this._trigger("changeDate");var i;this.isInput?i=this.element:this.component&&(i=this.element.find("input")),i&&i.change(),!this.o.autoclose||e&&"date"!==e||this.hide()},moveMonth:function(t,i){if(!t)return e;if(!i)return t;var a,s,n=new Date(t.valueOf()),r=n.getUTCDate(),o=n.getUTCMonth(),h=Math.abs(i);if(i=i>0?1:-1,1===h)s=-1===i?function(){return n.getUTCMonth()===o}:function(){return n.getUTCMonth()!==a},a=o+i,n.setUTCMonth(a),(0>a||a>11)&&(a=(a+12)%12);else{for(var d=0;h>d;d++)n=this.moveMonth(n,i);a=n.getUTCMonth(),n.setUTCDate(r),s=function(){return a!==n.getUTCMonth()}}for(;s();)n.setUTCDate(--r),n.setUTCMonth(a);return n},moveYear:function(t,e){return this.moveMonth(t,12*e)},dateWithinRange:function(t){return t>=this.o.startDate&&t<=this.o.endDate},keydown:function(t){if(this.picker.is(":not(:visible)"))return void(27===t.keyCode&&this.show());var e,i,s,n=!1,r=this.focusDate||this.viewDate;switch(t.keyCode){case 27:this.focusDate?(this.focusDate=null,this.viewDate=this.dates.get(-1)||this.viewDate,this.fill()):this.hide(),t.preventDefault();break;case 37:case 39:if(!this.o.keyboardNavigation)break;e=37===t.keyCode?-1:1,t.ctrlKey?(i=this.moveYear(this.dates.get(-1)||a(),e),s=this.moveYear(r,e),this._trigger("changeYear",this.viewDate)):t.shiftKey?(i=this.moveMonth(this.dates.get(-1)||a(),e),s=this.moveMonth(r,e),this._trigger("changeMonth",this.viewDate)):(i=new Date(this.dates.get(-1)||a()),i.setUTCDate(i.getUTCDate()+e),s=new Date(r),s.setUTCDate(r.getUTCDate()+e)),this.dateWithinRange(i)&&(this.focusDate=this.viewDate=s,this.setValue(),this.fill(),t.preventDefault());break;case 38:case 40:if(!this.o.keyboardNavigation)break;e=38===t.keyCode?-1:1,t.ctrlKey?(i=this.moveYear(this.dates.get(-1)||a(),e),s=this.moveYear(r,e),this._trigger("changeYear",this.viewDate)):t.shiftKey?(i=this.moveMonth(this.dates.get(-1)||a(),e),s=this.moveMonth(r,e),this._trigger("changeMonth",this.viewDate)):(i=new Date(this.dates.get(-1)||a()),i.setUTCDate(i.getUTCDate()+7*e),s=new Date(r),s.setUTCDate(r.getUTCDate()+7*e)),this.dateWithinRange(i)&&(this.focusDate=this.viewDate=s,this.setValue(),this.fill(),t.preventDefault());break;case 32:break;case 13:r=this.focusDate||this.dates.get(-1)||this.viewDate,this._toggle_multidate(r),n=!0,this.focusDate=null,this.viewDate=this.dates.get(-1)||this.viewDate,this.setValue(),this.fill(),this.picker.is(":visible")&&(t.preventDefault(),this.o.autoclose&&this.hide());break;case 9:this.focusDate=null,this.viewDate=this.dates.get(-1)||this.viewDate,this.fill(),this.hide()}if(n){this._trigger(this.dates.length?"changeDate":"clearDate");var o;this.isInput?o=this.element:this.component&&(o=this.element.find("input")),o&&o.change()}},showMode:function(t){t&&(this.viewMode=Math.max(this.o.minViewMode,Math.min(2,this.viewMode+t))),this.picker.find(">div").hide().filter(".datepicker-"+g.modes[this.viewMode].clsName).css("display","block"),this.updateNavArrows()}};var l=function(e,i){this.element=t(e),this.inputs=t.map(i.inputs,function(t){return t.jquery?t[0]:t}),delete i.inputs,t(this.inputs).datepicker(i).bind("changeDate",t.proxy(this.dateUpdated,this)),this.pickers=t.map(this.inputs,function(e){return t(e).data("datepicker")}),this.updateDates()};l.prototype={updateDates:function(){this.dates=t.map(this.pickers,function(t){return t.getUTCDate()}),this.updateRanges()},updateRanges:function(){var e=t.map(this.dates,function(t){return t.valueOf()});t.each(this.pickers,function(t,i){i.setRange(e)})},dateUpdated:function(e){if(!this.updating){this.updating=!0;var i=t(e.target).data("datepicker"),a=i.getUTCDate(),s=t.inArray(e.target,this.inputs),n=this.inputs.length;if(-1!==s){if(t.each(this.pickers,function(t,e){e.getUTCDate()||e.setUTCDate(a)}),a<this.dates[s])for(;s>=0&&a<this.dates[s];)this.pickers[s--].setUTCDate(a);else if(a>this.dates[s])for(;n>s&&a>this.dates[s];)this.pickers[s++].setUTCDate(a);this.updateDates(),delete this.updating}}},remove:function(){t.map(this.pickers,function(t){t.remove()}),delete this.element.data().datepicker}};var c=t.fn.datepicker;t.fn.datepicker=function(i){var a=Array.apply(null,arguments);a.shift();var s;return this.each(function(){var o=t(this),h=o.data("datepicker"),c="object"==typeof i&&i;if(!h){var p=n(this,"date"),f=t.extend({},u,p,c),g=r(f.language),D=t.extend({},u,g,p,c);if(o.is(".input-daterange")||D.inputs){var m={inputs:D.inputs||o.find("input").toArray()};o.data("datepicker",h=new l(this,t.extend(D,m)))}else o.data("datepicker",h=new d(this,D))}return"string"==typeof i&&"function"==typeof h[i]&&(s=h[i].apply(h,a),s!==e)?!1:void 0}),s!==e?s:this};var u=t.fn.datepicker.defaults={autoclose:!1,beforeShowDay:t.noop,calendarWeeks:!1,clearBtn:!1,daysOfWeekDisabled:[],endDate:1/0,forceParse:!0,format:"mm/dd/yyyy",keyboardNavigation:!0,language:"en",minViewMode:0,multidate:!1,multidateSeparator:",",orientation:"auto",rtl:!1,startDate:-1/0,startView:0,todayBtn:!1,todayHighlight:!1,weekStart:0},p=t.fn.datepicker.locale_opts=["format","rtl","weekStart"];t.fn.datepicker.Constructor=d;var f=t.fn.datepicker.dates={en:{days:["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"],daysShort:["Sun","Mon","Tue","Wed","Thu","Fri","Sat","Sun"],daysMin:["Su","Mo","Tu","We","Th","Fr","Sa","Su"],months:["January","February","March","April","May","June","July","August","September","October","November","December"],monthsShort:["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"],today:"Today",clear:"Clear"}},g={modes:[{clsName:"days",navFnc:"Month",navStep:1},{clsName:"months",navFnc:"FullYear",navStep:1},{clsName:"years",navFnc:"FullYear",navStep:10}],isLeapYear:function(t){return t%4===0&&t%100!==0||t%400===0},getDaysInMonth:function(t,e){return[31,g.isLeapYear(t)?29:28,31,30,31,30,31,31,30,31,30,31][e]},validParts:/dd?|DD?|mm?|MM?|yy(?:yy)?/g,nonpunctuation:/[^ -\/:-@\[\u3400-\u9fff-`{-~\t\n\r]+/g,parseFormat:function(t){var e=t.replace(this.validParts,"\x00").split("\x00"),i=t.match(this.validParts);if(!e||!e.length||!i||0===i.length)throw new Error("Invalid date format.");return{separators:e,parts:i}},parseDate:function(a,s,n){function r(){var t=this.slice(0,u[l].length),e=u[l].slice(0,t.length);return t===e}if(!a)return e;if(a instanceof Date)return a;"string"==typeof s&&(s=g.parseFormat(s));var o,h,l,c=/([\-+]\d+)([dmwy])/,u=a.match(/([\-+]\d+)([dmwy])/g);if(/^[\-+]\d+[dmwy]([\s,]+[\-+]\d+[dmwy])*$/.test(a)){for(a=new Date,l=0;l<u.length;l++)switch(o=c.exec(u[l]),h=parseInt(o[1]),o[2]){case"d":a.setUTCDate(a.getUTCDate()+h);break;case"m":a=d.prototype.moveMonth.call(d.prototype,a,h);break;case"w":a.setUTCDate(a.getUTCDate()+7*h);break;case"y":a=d.prototype.moveYear.call(d.prototype,a,h)}return i(a.getUTCFullYear(),a.getUTCMonth(),a.getUTCDate(),0,0,0)}u=a&&a.match(this.nonpunctuation)||[],a=new Date;var p,D,m={},v=["yyyy","yy","M","MM","m","mm","d","dd"],y={yyyy:function(t,e){return t.setUTCFullYear(e)},yy:function(t,e){return t.setUTCFullYear(2e3+e)},m:function(t,e){if(isNaN(t))return t;for(e-=1;0>e;)e+=12;for(e%=12,t.setUTCMonth(e);t.getUTCMonth()!==e;)t.setUTCDate(t.getUTCDate()-1);return t},d:function(t,e){return t.setUTCDate(e)}};y.M=y.MM=y.mm=y.m,y.dd=y.d,a=i(a.getFullYear(),a.getMonth(),a.getDate(),0,0,0);var w=s.parts.slice();if(u.length!==w.length&&(w=t(w).filter(function(e,i){return-1!==t.inArray(i,v)}).toArray()),u.length===w.length){var k;for(l=0,k=w.length;k>l;l++){if(p=parseInt(u[l],10),o=w[l],isNaN(p))switch(o){case"MM":D=t(f[n].months).filter(r),p=t.inArray(D[0],f[n].months)+1;break;case"M":D=t(f[n].monthsShort).filter(r),p=t.inArray(D[0],f[n].monthsShort)+1}m[o]=p}var C,_;for(l=0;l<v.length;l++)_=v[l],_ in m&&!isNaN(m[_])&&(C=new Date(a),y[_](C,m[_]),isNaN(C)||(a=C))}return a},formatDate:function(e,i,a){if(!e)return"";"string"==typeof i&&(i=g.parseFormat(i));var s={d:e.getUTCDate(),D:f[a].daysShort[e.getUTCDay()],DD:f[a].days[e.getUTCDay()],m:e.getUTCMonth()+1,M:f[a].monthsShort[e.getUTCMonth()],MM:f[a].months[e.getUTCMonth()],yy:e.getUTCFullYear().toString().substring(2),yyyy:e.getUTCFullYear()};s.dd=(s.d<10?"0":"")+s.d,s.mm=(s.m<10?"0":"")+s.m,e=[];for(var n=t.extend([],i.separators),r=0,o=i.parts.length;o>=r;r++)n.length&&e.push(n.shift()),e.push(s[i.parts[r]]);return e.join("")},headTemplate:'<thead><tr><th class="prev">&laquo;</th><th colspan="5" class="datepicker-switch"></th><th class="next">&raquo;</th></tr></thead>',contTemplate:'<tbody><tr><td colspan="7"></td></tr></tbody>',footTemplate:'<tfoot><tr><th colspan="7" class="today"></th></tr><tr><th colspan="7" class="clear"></th></tr></tfoot>'};g.template='<div class="datepicker"><div class="datepicker-days"><table class=" table-condensed">'+g.headTemplate+"<tbody></tbody>"+g.footTemplate+'</table></div><div class="datepicker-months"><table class="table-condensed">'+g.headTemplate+g.contTemplate+g.footTemplate+'</table></div><div class="datepicker-years"><table class="table-condensed">'+g.headTemplate+g.contTemplate+g.footTemplate+"</table></div></div>",t.fn.datepicker.DPGlobal=g,t.fn.datepicker.noConflict=function(){return t.fn.datepicker=c,this},t(document).on("focus.datepicker.data-api click.datepicker.data-api",'[data-provide="datepicker"]',function(e){var i=t(this);i.data("datepicker")||(e.preventDefault(),i.datepicker("show"))}),t(function(){t('[data-provide="datepicker-inline"]').datepicker()})}(window.jQuery),function(t){t.fn.datepicker.dates.de={days:["Sonntag","Montag","Dienstag","Mittwoch","Donnerstag","Freitag","Samstag","Sonntag"],daysShort:["Son","Mon","Die","Mit","Don","Fre","Sam","Son"],daysMin:["So","Mo","Di","Mi","Do","Fr","Sa","So"],months:["Januar","Februar","März","April","Mai","Juni","Juli","August","September","Oktober","November","Dezember"],monthsShort:["Jan","Feb","Mär","Apr","Mai","Jun","Jul","Aug","Sep","Okt","Nov","Dez"],today:"Heute",clear:"Löschen",weekStart:1,format:"dd.mm.yyyy"}}(jQuery);


var newInformationCounter = 0, //Counter used to create unique identifiers for new input fields
    userSubmitButton,
    userDeleteButton,
    userList;

//Add form fields from existing key and value to target
function addInformation(target, key, value, edit) {
    var name = key;
    key = '_old' + target + key;
    var removeButton = '';
    if(!edit)
    {
        removeButton = '<a id="remove_' + key + '">Remove '+ name + '</a>';
    }
    $(target).append('' +
        '<div id="' + key + '_field" class="form-group">' +
            '<label class="col-sm-3 control-label">' + name + '</label>' +
            '<div class="col-sm-9">' +
                '<input name="' + key + '" class="form-control" value="' + value + '" type="text"/>' +
                removeButton +
            '</div>' +
        '</div>');

    if(!edit)
    {
        $('#remove_' + key).click(function(e){
            $('#' + key + '_field').remove();
        });
    }
}

//Create new form fields without existing key or value
function addNewInformation(target) {
    var key = '_new' + target + newInformationCounter;
    newInformationCounter++;
    removeButton = '<a id="remove_' + key + '">Remove field</a>';

    $(target).append('' +
    '<div id="' + key + '_field" class="form-group">' +
        '<label class="col-sm-3 control-label">New custom field</label>' +
        '<div class="col-sm-9">' +
            '<input name="'+ key + '_key" class="form-control" placeholder="Key" type="text"/>' +
            '<input name="'+ key + '_value" class="form-control" placeholder="Value" type="text"/>' +
            removeButton +
        '</div>' +
    '</div>');

    $('#remove_' + key).click(function(e){
        $('#' + key + '_field').remove();
    });
}

//Reset the user form
function resetUserForm(doNotHideDeleteButton) {
    $('#firstName').val('');
    $('#lastName').val('');
    $('#email').val('');
    $('#password').val('');
    $('#birthday').val('');
    $("#gender").val("default");

    $('#street').val('');
    $('#streetNumber').val('');
    $('#zip').val('');
    $('#city').val('');
    $('#country').val('');

    $('#activeMemberSince').val('');
    $('#passiveMemberSince').val('');
    $('#resignationDate').val('');

    $('#iban').val('');
    $('#bic').val('');

    $('#divisions')[0].selectize.clear();

    //Hiding private and public information
    $('.publicInformation').addClass('hidden');
    $('.privateInformation').addClass('hidden');
    $('.addPrivateInformation').addClass('hidden');
    $('.addPublicInformation').addClass('hidden');

    //Hide & reset Password field
    $('#newUser').addClass("hidden");
    $('#password').val('');

    //Clear submit button
    $('#newUserButton').addClass('hidden');
    $('#oldUserButton').addClass('hidden');

    //Re-enable submit button
    userSubmitButton.enable();

    //Re-enable form
    $("#userForm :input").prop("disabled", false);
    $('#divisions')[0].selectize.enable();

    if(!doNotHideDeleteButton) {
        //Hide delete button
        $('#userDelete').addClass('hidden');
    }

    //Hide and clear heading
    $('#newUserHeading').addClass("hidden");
    $('#oldUserHeading').addClass("hidden");
    $('#oldUserHeadingName').empty();

    //Reseting previous validation annotation
    $('#userForm').data('bootstrapValidator').resetForm();
}

//Disabling the user form, if a user is not allowed to manipulate the user
function disableUserForm(){
    userSubmitButton.disable();
    $("#userForm :input").prop("disabled", true);
    $('#divisions')[0].selectize.disable();
}

//Loading a user's information into the form
function loadUser(email) {
    //Sending JSON request with the email as parameter to get the user details
    $.getJSON("/user/getUser", {email: email}, function(user) {
        resetUserForm();
        //Filling obvious fields
        $('#firstName').val(user.firstName);
        $('#lastName').val(user.lastName);
        $('#email').val(user.email);

        $("#gender").val(user.gender);
        $('#country').val(user.country);

        if(user.activeSince)
        {
            //Parsing date from response
            var date = new Date(0);
            date.setUTCDate(user.activeSince.dayOfMonth);
            date.setUTCMonth(user.activeSince.monthValue - 1); //LocalDate is not a 0 starting index at the month
            date.setUTCFullYear(user.activeSince.year);
            $('#activeMemberSince').datepicker('setUTCDate', date);
        }

        if(user.passiveSince)
        {
            //Parsing date from response
            var date = new Date(0);
            date.setUTCDate(user.passiveSince.dayOfMonth);
            date.setUTCMonth(user.passiveSince.monthValue - 1); //LocalDate is not a 0 starting index at the month
            date.setUTCFullYear(user.passiveSince.year);
            $('#passiveMemberSince').datepicker('setUTCDate', date);
        }

        if(user.resignationDate)
        {
            //Parsing date from response
            var date = new Date(0);
            date.setUTCDate(user.resignationDate.dayOfMonth);
            date.setUTCMonth(user.resignationDate.monthValue - 1); //LocalDate is not a 0 starting index at the month
            date.setUTCFullYear(user.resignationDate.year);
            $('#resignationDate').datepicker('setUTCDate', date);
        }

        if(!user.administrationNotAllowedMessage) //No message means he is allowed to administrate, everything happening in this block shouldn't be part of the response anyway
        {
            //Show custom field headings and enable buttons
            $('.publicInformation').removeClass('hidden');
            $('.privateInformation').removeClass('hidden');
            $('.addPublicInformation').removeClass('hidden');
            $('.addPrivateInformation').removeClass('hidden');

            $('#addPrivateInformationButton').click(function(e){
                addNewInformation('.privateInformation');
            });

            $('#addPublicInformationButton').click(function(e){
                addNewInformation('.publicInformation');
            });

            $('#iban').val(user.iban);
            $('#bic').val(user.bic);
            $('#street').val(user.street);
            $('#streetNumber').val(user.streetNumber);
            $('#zip').val(user.zipCode);
            $('#city').val(user.city);

            if(user.birthday)
            {
                //Parsing date from response
                var date = new Date(0);
                date.setUTCDate(user.birthday.dayOfMonth);
                date.setUTCMonth(user.birthday.monthValue - 1); //LocalDate is not a 0 starting index at the month
                date.setUTCFullYear(user.birthday.year);
                $('#birthday').datepicker('setUTCDate', date);
            }

            //Inserting private information if there are any
            if (user.privateInformation) {
                $('.privateInformation').removeClass('hidden');
                $.each(user.privateInformation, function (key, value) {
                    addInformation('.privateInformation', key, value, user.administrationNotAllowedMessage);
                });
            }
        } else //If not allowed to edit disable some stuff and tell user
        {
            showMessage(user.administrationNotAllowedMessage, 'warning', 'icon_error-triangle_alt');
            disableUserForm();
        }

        if(user.memberSince)
        {
            //Parsing date from response
            var date = new Date(0);
            date.setUTCDate(user.memberSince.dayOfMonth);
            date.setUTCMonth(user.memberSince.monthValue - 1); //LocalDate is not a 0 starting index at the month
            date.setUTCFullYear(user.memberSince.year);
            $('#activeMemberSince').datepicker('setUTCDate', date);
        }

        //Fill division list
        if (user.divisions) {
            $.each(user.divisions, function (index, division) {
                $('#divisions')[0].selectize.addItem(division.name);
            });
        }

        //Inserting public information if there are any
        if (user.publicInformation) {
            $('.publicInformation').removeClass('hidden');
            $.each(user.publicInformation, function (key, value) {
                addInformation('.publicInformation', key, value, user.administrationNotAllowedMessage);
            });
        }

        //Show important fields
        $('#userDelete').removeClass('hidden');
        $('#oldUserHeading').removeClass("hidden");
        $('#oldUserHeadingName').text('<' + user.email + '>');
        $('#oldUserButton').removeClass('hidden');
        $('#userFlag').val(user.email);
    });
}

//Set up everything to create a new user
function loadNewUser(doNotHideDeleteButton) {
    resetUserForm(doNotHideDeleteButton);
    $('#userFlag').val("true");
    $('#newUserHeading').removeClass('hidden');
    $('#newUser').removeClass('hidden');

    $('#newUserButton').removeClass('hidden');
    userSubmitButton.enable();
    $('#firstName').focus();
}

//(Re-)Load the user list on the left
function loadUserList() {
    userList.clear();
    $("#user-list-loading").addClass('heartbeat');
    //Loading user list through ajax request
    $.getJSON("/user/getUser", function (data) {
        $("#user-list-loading").removeClass('heartbeat');
        userList.add(data);
    });
}

//This function is called as soon as the tab is shown. If necessary it is loading all required resources.
function loadUserPage() {
    if(!$('#divisions')[0].selectize) {
        //Configuring division input field
        $('#divisions').selectize({
            persist: false,
            createOnBlur: true,
            create: false, //Not allowing the creation of user specific items
            hideSelected: true, //If an option is allready in the list it is hidden
            preload: true, //Loading data immidiately (if user is loaded without loading the available divisions, the added divisions get removed because selectize thinks they are not valid)
            valueField: 'name',
            labelField: 'name',
            searchField: 'name',
            load: function (query, callback) {
                $.ajax({
                    url: '/division/getDivision',
                    type: 'GET',
                    data: {
                        term: query
                    },
                    error: function () {
                        callback();
                    },
                    success: function (data) {
                        callback(data);
                    }
                });
            }
        });
    }

    if(!userList) {
        //Configuring fuzzy-search on user list.
        var listOptions = {
            valueNames: ['firstName', 'lastName', 'email'],
            item: '<li class="list-item"><h3><span class="firstName"></span> <span class="lastName"></span></h3><p class="email"></p></li>',
            plugins: [ListFuzzySearch()]
        }

        //Creating user list
        userList = new List('user-list', listOptions);

        //When items are added to the list the listener for the list items need to be updated.
        userList.on("updated", function () {
            $("li.list-item").click(function (e) {
                //Get the email as identification of the selected user and loading the user into the form
                userSubmitButton.startAnimation();
                loadUser($(this).children(".email").text());
                userSubmitButton.stopAnimation(1);
            });
        });
    }

    if(!$('#userForm').data('bootstrapValidator')) {
        //Enable bootstrap validator
        $('#userForm').bootstrapValidator({
            excluded: [':disabled', ':hidden', ':not(:visible)']
        }) //The constrains are configured within the HTML
            .on('success.form.bv', function (e) { //The submission function
                // Prevent form submission
                e.preventDefault();
                //Starting button animation
                userSubmitButton.startAnimation();
                //Send the serialized form
                $.ajax({
                    url: '/user',
                    type: 'POST',
                    data: $(e.target).serialize(),
                    error: function (response) {
                        userSubmitButton.stopAnimation(-1);
                        showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                    },
                    success: function (response) {
                        userSubmitButton.stopAnimation(0);
                        showMessage(response, 'success', 'icon_check');
                        loadUserList();
                    }
                });
            });
    }

    if(!userSubmitButton) {
        //Enabling progress button
        userSubmitButton = new UIProgressButton(document.getElementById('userSubmitButton'));
    }

    if(!userDeleteButton) {
        //Enabling progress button
        userDeleteButton = new UIProgressButton(document.getElementById('userDelete'));
        $('#userDeleteButton').click(function(e){
            e.preventDefault();
            userDeleteButton.startAnimation();
            $.ajax({
                url: '/user/deleteUser',
                type: 'POST',
                data: {
                    email: $('#userFlag').val()
                },
                error: function (response) {
                    userDeleteButton.stopAnimation(-1);
                    showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                },
                success: function (response) {
                    userDeleteButton.stopAnimation(0);
                    showMessage(response, 'success', 'icon_check');
                    loadNewUser(true);
                    loadUserList();
                }
            });
        })
    }

    if(!($('#activeMemberSince').data().datepicker && $('#birthday').data().datepicker && $('#passiveMemberSince').data().datepicker && $('#resignationDate').data().datepicker)) {
        //Enable Datepicker
        var datepickerOptions = {
            format: "dd/mm/yyyy",
            weekStart: 1,
            todayHighlight: true
        };
        $('#birthday').datepicker(datepickerOptions);
        $('#activeMemberSince').datepicker(datepickerOptions);
        $('#passiveMemberSince').datepicker(datepickerOptions);
        $('#resignationDate').datepicker(datepickerOptions);
    }


    $('#addUser').click(function (e) {
        loadNewUser();
    });

    loadUserList();
    loadNewUser();
}