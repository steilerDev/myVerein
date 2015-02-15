/**
 * Document   : myVerein.division.js
 * Description: This JavaScript file contains all methods needed by the division page. A minified version of jqTree is included
 * Copyright  : (c) 2014 Frank Steiler <frank@steilerdev.de>
 * License    : GNU General Public License v2.0
 */

//jqTree
(function(){var e,t,n,o,r,i,s,d,l,u,a,h,p,c,_,f,g,m,v,y,N,S,w,F,C,D=[].slice,E={}.hasOwnProperty,T=function(e,t){function n(){this.constructor=e}for(var o in t)E.call(t,o)&&(e[o]=t[o]);return n.prototype=t.prototype,e.prototype=new n,e.__super__=t.prototype,e};F="0.22.0",e=this.jQuery,m=function(){function t(t,n){this.$el=e(t),this.options=e.extend({},this.defaults,n)}return t.prototype.defaults={},t.prototype.destroy=function(){return this._deinit()},t.prototype._init=function(){return null},t.prototype._deinit=function(){return null},t.register=function(n,o){var r,i,s,d,l;return d=function(){return"simple_widget_"+o},l=function(n,o){var r;return r=e.data(n,o),r&&r instanceof t?r:null},i=function(t,o){var r,i,s,u,a,h;for(r=d(),a=0,h=t.length;h>a;a++)i=t[a],s=l(i,r),s||(u=new n(i,o),e.data(i,r)||e.data(i,r,u),u._init());return t},s=function(t){var n,o,r,i,s,u;for(n=d(),u=[],i=0,s=t.length;s>i;i++)o=t[i],r=l(o,n),r&&r.destroy(),u.push(e.removeData(o,n));return u},r=function(n,o,r){var i,s,l,u,a,h;for(s=null,a=0,h=n.length;h>a;a++)i=n[a],l=e.data(i,d()),l&&l instanceof t&&(u=l[o],u&&"function"==typeof u&&(s=u.apply(l,r)));return s},e.fn[o]=function(){var e,t,n,o,d;return n=arguments[0],t=2<=arguments.length?D.call(arguments,1):[],e=this,void 0===n||"object"==typeof n?(d=n,i(e,d)):"string"==typeof n&&"_"!==n[0]?(o=n,"destroy"===o?s(e):r(e,o,t)):void 0}},t}(),this.SimpleWidget=m,a=function(t){function n(){return n.__super__.constructor.apply(this,arguments)}return T(n,t),n.is_mouse_handled=!1,n.prototype._init=function(){return this.$el.bind("mousedown.mousewidget",e.proxy(this._mouseDown,this)),this.$el.bind("touchstart.mousewidget",e.proxy(this._touchStart,this)),this.is_mouse_started=!1,this.mouse_delay=0,this._mouse_delay_timer=null,this._is_mouse_delay_met=!0,this.mouse_down_info=null},n.prototype._deinit=function(){var t;return this.$el.unbind("mousedown.mousewidget"),this.$el.unbind("touchstart.mousewidget"),t=e(document),t.unbind("mousemove.mousewidget"),t.unbind("mouseup.mousewidget")},n.prototype._mouseDown=function(e){var t;if(1===e.which)return t=this._handleMouseDown(e,this._getPositionInfo(e)),t&&e.preventDefault(),t},n.prototype._handleMouseDown=function(e,t){return!n.is_mouse_handled&&(this.is_mouse_started&&this._handleMouseUp(t),this.mouse_down_info=t,this._mouseCapture(t))?(this._handleStartMouse(),this.is_mouse_handled=!0,!0):void 0},n.prototype._handleStartMouse=function(){var t;return t=e(document),t.bind("mousemove.mousewidget",e.proxy(this._mouseMove,this)),t.bind("touchmove.mousewidget",e.proxy(this._touchMove,this)),t.bind("mouseup.mousewidget",e.proxy(this._mouseUp,this)),t.bind("touchend.mousewidget",e.proxy(this._touchEnd,this)),this.mouse_delay?this._startMouseDelayTimer():void 0},n.prototype._startMouseDelayTimer=function(){return this._mouse_delay_timer&&clearTimeout(this._mouse_delay_timer),this._mouse_delay_timer=setTimeout(function(e){return function(){return e._is_mouse_delay_met=!0}}(this),this.mouse_delay),this._is_mouse_delay_met=!1},n.prototype._mouseMove=function(e){return this._handleMouseMove(e,this._getPositionInfo(e))},n.prototype._handleMouseMove=function(e,t){return this.is_mouse_started?(this._mouseDrag(t),e.preventDefault()):this.mouse_delay&&!this._is_mouse_delay_met?!0:(this.is_mouse_started=this._mouseStart(this.mouse_down_info)!==!1,this.is_mouse_started?this._mouseDrag(t):this._handleMouseUp(t),!this.is_mouse_started)},n.prototype._getPositionInfo=function(e){return{page_x:e.pageX,page_y:e.pageY,target:e.target,original_event:e}},n.prototype._mouseUp=function(e){return this._handleMouseUp(this._getPositionInfo(e))},n.prototype._handleMouseUp=function(t){var n;n=e(document),n.unbind("mousemove.mousewidget"),n.unbind("touchmove.mousewidget"),n.unbind("mouseup.mousewidget"),n.unbind("touchend.mousewidget"),this.is_mouse_started&&(this.is_mouse_started=!1,this._mouseStop(t))},n.prototype._mouseCapture=function(){return!0},n.prototype._mouseStart=function(){return null},n.prototype._mouseDrag=function(){return null},n.prototype._mouseStop=function(){return null},n.prototype.setMouseDelay=function(e){return this.mouse_delay=e},n.prototype._touchStart=function(e){var t;if(!(e.originalEvent.touches.length>1))return t=e.originalEvent.changedTouches[0],this._handleMouseDown(e,this._getPositionInfo(t))},n.prototype._touchMove=function(e){var t;if(!(e.originalEvent.touches.length>1))return t=e.originalEvent.changedTouches[0],this._handleMouseMove(e,this._getPositionInfo(t))},n.prototype._touchEnd=function(e){var t;if(!(e.originalEvent.touches.length>1))return t=e.originalEvent.changedTouches[0],this._handleMouseUp(this._getPositionInfo(t))},n}(m),this.Tree={},e=this.jQuery,c={getName:function(e){return c.strings[e-1]},nameToIndex:function(e){var t,n,o;for(t=n=1,o=c.strings.length;o>=1?o>=n:n>=o;t=o>=1?++n:--n)if(c.strings[t-1]===e)return t;return 0}},c.BEFORE=1,c.AFTER=2,c.INSIDE=3,c.NONE=4,c.strings=["before","after","inside","none"],this.Tree.Position=c,h=function(){function t(e,n,o){null==n&&(n=!1),null==o&&(o=t),this.setData(e),this.children=[],this.parent=null,n&&(this.id_mapping={},this.tree=this,this.node_class=o)}return t.prototype.setData=function(e){var t,n,o;if("object"!=typeof e)return this.name=e;o=[];for(t in e)n=e[t],o.push("label"===t?this.name=n:this[t]=n);return o},t.prototype.initFromData=function(e){var t,n;return n=function(e){return function(n){return e.setData(n),n.children?t(n.children):void 0}}(this),t=function(e){return function(t){var n,o,r,i;for(r=0,i=t.length;i>r;r++)n=t[r],o=new e.tree.node_class(""),o.initFromData(n),e.addChild(o);return null}}(this),n(e),null},t.prototype.loadFromData=function(e){var t,n,o,r;for(this.removeChildren(),o=0,r=e.length;r>o;o++)n=e[o],t=new this.tree.node_class(n),this.addChild(t),"object"==typeof n&&n.children&&t.loadFromData(n.children);return null},t.prototype.addChild=function(e){return this.children.push(e),e._setParent(this)},t.prototype.addChildAtPosition=function(e,t){return this.children.splice(t,0,e),e._setParent(this)},t.prototype._setParent=function(e){return this.parent=e,this.tree=e.tree,this.tree.addNodeToIndex(this)},t.prototype.removeChild=function(e){return e.removeChildren(),this._removeChild(e)},t.prototype._removeChild=function(e){return this.children.splice(this.getChildIndex(e),1),this.tree.removeNodeFromIndex(e)},t.prototype.getChildIndex=function(t){return e.inArray(t,this.children)},t.prototype.hasChildren=function(){return 0!==this.children.length},t.prototype.isFolder=function(){return this.hasChildren()||this.load_on_demand},t.prototype.iterate=function(e){var t;return t=function(n){return function(o,r){var i,s,d,l,u;if(o.children){for(u=o.children,d=0,l=u.length;l>d;d++)i=u[d],s=e(i,r),n.hasChildren()&&s&&t(i,r+1);return null}}}(this),t(this,0),null},t.prototype.moveNode=function(e,t,n){return e.isParentOf(t)?void 0:(e.parent._removeChild(e),n===c.AFTER?t.parent.addChildAtPosition(e,t.parent.getChildIndex(t)+1):n===c.BEFORE?t.parent.addChildAtPosition(e,t.parent.getChildIndex(t)):n===c.INSIDE?t.addChildAtPosition(e,0):void 0)},t.prototype.getData=function(){var e;return(e=function(){return function(t){var n,o,r,i,s,d,l;for(n=[],d=0,l=t.length;l>d;d++){r=t[d],i={};for(o in r)s=r[o],"parent"!==o&&"children"!==o&&"element"!==o&&"tree"!==o&&Object.prototype.hasOwnProperty.call(r,o)&&(i[o]=s);r.hasChildren()&&(i.children=e(r.children)),n.push(i)}return n}}(this))(this.children)},t.prototype.getNodeByName=function(e){var t;return t=null,this.iterate(function(n){return n.name===e?(t=n,!1):!0}),t},t.prototype.addAfter=function(e){var t,n;return this.parent?(n=new this.tree.node_class(e),t=this.parent.getChildIndex(this),this.parent.addChildAtPosition(n,t+1),n):null},t.prototype.addBefore=function(e){var t,n;return this.parent?(n=new this.tree.node_class(e),t=this.parent.getChildIndex(this),this.parent.addChildAtPosition(n,t),n):null},t.prototype.addParent=function(e){var t,n,o,r,i,s;if(this.parent){for(n=new this.tree.node_class(e),n._setParent(this.tree),o=this.parent,s=o.children,r=0,i=s.length;i>r;r++)t=s[r],n.addChild(t);return o.children=[],o.addChild(n),n}return null},t.prototype.remove=function(){return this.parent?(this.parent.removeChild(this),this.parent=null):void 0},t.prototype.append=function(e){var t;return t=new this.tree.node_class(e),this.addChild(t),t},t.prototype.prepend=function(e){var t;return t=new this.tree.node_class(e),this.addChildAtPosition(t,0),t},t.prototype.isParentOf=function(e){var t;for(t=e.parent;t;){if(t===this)return!0;t=t.parent}return!1},t.prototype.getLevel=function(){var e,t;for(e=0,t=this;t.parent;)e+=1,t=t.parent;return e},t.prototype.getNodeById=function(e){return this.id_mapping[e]},t.prototype.addNodeToIndex=function(e){return null!=e.id?this.id_mapping[e.id]=e:void 0},t.prototype.removeNodeFromIndex=function(e){return null!=e.id?delete this.id_mapping[e.id]:void 0},t.prototype.removeChildren=function(){return this.iterate(function(e){return function(t){return e.tree.removeNodeFromIndex(t),!0}}(this)),this.children=[]},t.prototype.getPreviousSibling=function(){var e;return this.parent?(e=this.parent.getChildIndex(this)-1,e>=0?this.parent.children[e]:null):null},t.prototype.getNextSibling=function(){var e;return this.parent?(e=this.parent.getChildIndex(this)+1,e<this.parent.children.length?this.parent.children[e]:null):null},t.prototype.getNodesByProperty=function(e,t){return this.filter(function(n){return n[e]===t})},t.prototype.filter=function(e){var t;return t=[],this.iterate(function(n){return e(n)&&t.push(n),!0}),t},t}(),this.Tree.Node=h,r=function(){function t(e){this.tree_widget=e,this.opened_icon_element=this.createButtonElement(e.options.openedIcon),this.closed_icon_element=this.createButtonElement(e.options.closedIcon)}return t.prototype.render=function(e){return e&&e.parent?this.renderFromNode(e):this.renderFromRoot()},t.prototype.renderNode=function(t){var n,o,r;return e(t.element).remove(),o=new p(t.parent,this.tree_widget),n=this.createLi(t),this.attachNodeData(t,n),r=t.getPreviousSibling(),r?e(r.element).after(n):o.getUl().prepend(n),t.children?this.renderFromNode(t):void 0},t.prototype.renderFromRoot=function(){var e;return e=this.tree_widget.element,e.empty(),this.createDomElements(e[0],this.tree_widget.tree.children,!0,!0)},t.prototype.renderFromNode=function(e){var t;return t=this.tree_widget._getNodeElementForNode(e),t.getUl().remove(),this.createDomElements(t.$element[0],e.children,!1,!1)},t.prototype.createDomElements=function(e,t,n){var o,r,i,s,d;for(i=this.createUl(n),e.appendChild(i),s=0,d=t.length;d>s;s++)o=t[s],r=this.createLi(o),i.appendChild(r),this.attachNodeData(o,r),o.hasChildren()&&this.createDomElements(r,o.children,!1,o.is_open);return null},t.prototype.attachNodeData=function(t,n){return t.element=n,e(n).data("node",t)},t.prototype.createUl=function(e){var t,n;return t=e?"jqtree-tree":"",n=document.createElement("ul"),n.className="jqtree_common "+t,n},t.prototype.createLi=function(t){var n;return n=t.isFolder()?this.createFolderLi(t):this.createNodeLi(t),this.tree_widget.options.onCreateLi&&this.tree_widget.options.onCreateLi(t,e(n)),n},t.prototype.createFolderLi=function(e){var t,n,o,r,i,s,d,l;return t=this.getButtonClasses(e),i=this.getFolderClasses(e),r=this.escapeIfNecessary(e.name),s=e.is_open?this.opened_icon_element:this.closed_icon_element,d=document.createElement("li"),d.className="jqtree_common "+i,o=document.createElement("div"),o.className="jqtree-element jqtree_common",d.appendChild(o),n=document.createElement("a"),n.className="jqtree_common "+t,n.appendChild(s.cloneNode()),o.appendChild(n),l=document.createElement("span"),l.className="jqtree_common jqtree-title jqtree-title-folder",o.appendChild(l),l.innerHTML=r,d},t.prototype.createNodeLi=function(e){var t,n,o,r,i,s;return i=["jqtree_common"],this.tree_widget.select_node_handler&&this.tree_widget.select_node_handler.isNodeSelected(e)&&i.push("jqtree-selected"),t=i.join(" "),o=this.escapeIfNecessary(e.name),r=document.createElement("li"),r.className=t,n=document.createElement("div"),n.className="jqtree-element jqtree_common",r.appendChild(n),s=document.createElement("span"),s.className="jqtree-title jqtree_common",s.innerHTML=o,n.appendChild(s),r},t.prototype.getButtonClasses=function(e){var t;return t=["jqtree-toggler"],e.is_open||t.push("jqtree-closed"),t.join(" ")},t.prototype.getFolderClasses=function(e){var t;return t=["jqtree-folder"],e.is_open||t.push("jqtree-closed"),this.tree_widget.select_node_handler&&this.tree_widget.select_node_handler.isNodeSelected(e)&&t.push("jqtree-selected"),t.join(" ")},t.prototype.escapeIfNecessary=function(e){return this.tree_widget.options.autoEscape?N(e):e},t.prototype.createButtonElement=function(t){var n;return"string"==typeof t?(n=document.createElement("div"),n.innerHTML=t,document.createTextNode(n.innerHTML)):e(t)[0]},t}(),l=function(t){function o(){return o.__super__.constructor.apply(this,arguments)}return T(o,t),o.prototype.defaults={autoOpen:!1,saveState:!1,dragAndDrop:!1,selectable:!0,useContextMenu:!0,onCanSelectNode:null,onSetStateFromStorage:null,onGetStateFromStorage:null,onCreateLi:null,onIsMoveHandle:null,onCanMove:null,onCanMoveTo:null,onLoadFailed:null,autoEscape:!0,dataUrl:null,closedIcon:"&#x25ba;",openedIcon:"&#x25bc;",slide:!0,nodeClass:h,dataFilter:null,keyboardSupport:!0,openFolderDelay:500},o.prototype.toggle=function(e,t){return null==t&&(t=null),null===t&&(t=this.options.slide),e.is_open?this.closeNode(e,t):this.openNode(e,t)},o.prototype.getTree=function(){return this.tree},o.prototype.selectNode=function(e){return this._selectNode(e,!1)},o.prototype._selectNode=function(e,t){var n,o,r,i;if(null==t&&(t=!1),this.select_node_handler){if(n=function(t){return function(){return t.options.onCanSelectNode?t.options.selectable&&t.options.onCanSelectNode(e):t.options.selectable}}(this),r=function(t){return function(){var n;return n=e.parent,n&&n.parent&&!n.is_open?t.openNode(n,!1):void 0}}(this),i=function(e){return function(){return e.options.saveState?e.save_state_handler.saveState():void 0}}(this),!e)return this._deselectCurrentNode(),void i();if(n())return this.select_node_handler.isNodeSelected(e)?t&&(this._deselectCurrentNode(),this._triggerEvent("tree.select",{node:null,previous_node:e})):(o=this.getSelectedNode(),this._deselectCurrentNode(),this.addToSelection(e),this._triggerEvent("tree.select",{node:e,deselected_node:o}),r()),i()}},o.prototype.getSelectedNode=function(){return this.select_node_handler.getSelectedNode()},o.prototype.toJson=function(){return JSON.stringify(this.tree.getData())},o.prototype.loadData=function(e,t){return this._loadData(e,t)},o.prototype.loadDataFromUrl=function(t,n,o){return"string"!==e.type(t)&&(o=n,n=t,t=null),this._loadDataFromUrl(t,n,o)},o.prototype.reload=function(){return this.loadDataFromUrl()},o.prototype._loadDataFromUrl=function(t,n,o){var r,s,d,l,u,a;if(r=null,s=function(e){return function(){var t;return n?(t=new i(n,e),r=t.getLi()):r=e.element,r.addClass("jqtree-loading")}}(this),a=function(){return function(){return r?r.removeClass("jqtree-loading"):void 0}}(this),u=function(){return function(){return"string"===e.type(t)&&(t={url:t}),t.method?void 0:t.method="get"}}(this),d=function(t){return function(r){return a(),t._loadData(r,n),o&&e.isFunction(o)?o():void 0}}(this),l=function(n){return function(){return u(),e.ajax({url:t.url,data:t.data,type:t.method.toUpperCase(),cache:!1,dataType:"json",success:function(t){var o;return o=e.isArray(t)||"object"==typeof t?t:e.parseJSON(t),n.options.dataFilter&&(o=n.options.dataFilter(o)),d(o)},error:function(e){return a(),n.options.onLoadFailed?n.options.onLoadFailed(e):void 0}})}}(this),t||(t=this._getDataUrlInfo(n)),s(),t){if(!e.isArray(t))return l();d(t)}else a()},o.prototype._loadData=function(e,t){var n,o,r,i;if(e){if(this._triggerEvent("tree.load_data",{tree_data:e}),t){for(o=this.select_node_handler.getSelectedNodesUnder(t),r=0,i=o.length;i>r;r++)n=o[r],this.select_node_handler.removeFromSelection(n);t.loadFromData(e),t.load_on_demand=!1,this._refreshElements(t.parent)}else this._initTree(e);return this.isDragging()?this.dnd_handler.refresh():void 0}},o.prototype.getNodeById=function(e){return this.tree.getNodeById(e)},o.prototype.getNodeByName=function(e){return this.tree.getNodeByName(e)},o.prototype.openNode=function(e,t){return null==t&&(t=null),null===t&&(t=this.options.slide),this._openNode(e,t)},o.prototype._openNode=function(e,t,n){var o,r;if(null==t&&(t=!0),o=function(e){return function(t,n,o){var r;return r=new i(t,e),r.open(o,n)}}(this),e.isFolder()){if(e.load_on_demand)return this._loadFolderOnDemand(e,t,n);for(r=e.parent;r&&!r.is_open;)r.parent&&o(r,!1,null),r=r.parent;return o(e,t,n),this._saveState()}},o.prototype._loadFolderOnDemand=function(e,t,n){return null==t&&(t=!0),this._loadDataFromUrl(null,e,function(o){return function(){return o._openNode(e,t,n)}}(this))},o.prototype.closeNode=function(e,t){return null==t&&(t=null),null===t&&(t=this.options.slide),e.isFolder()?(new i(e,this).close(t),this._saveState()):void 0},o.prototype.isDragging=function(){return this.dnd_handler?this.dnd_handler.is_dragging:!1},o.prototype.refreshHitAreas=function(){return this.dnd_handler.refresh()},o.prototype.addNodeAfter=function(e,t){var n;return n=t.addAfter(e),this._refreshElements(t.parent),n},o.prototype.addNodeBefore=function(e,t){var n;return n=t.addBefore(e),this._refreshElements(t.parent),n},o.prototype.addParentNode=function(e,t){var n;return n=t.addParent(e),this._refreshElements(n.parent),n},o.prototype.removeNode=function(e){var t;return t=e.parent,t?(this.select_node_handler.removeFromSelection(e,!0),e.remove(),this._refreshElements(t.parent)):void 0},o.prototype.appendNode=function(e,t){var n,o;return t||(t=this.tree),n=t.isFolder(),o=t.append(e),this._refreshElements(n?t:t.parent),o},o.prototype.prependNode=function(e,t){var n;return t||(t=this.tree),n=t.prepend(e),this._refreshElements(t),n},o.prototype.updateNode=function(e,t){var n;return n=t.id&&t.id!==e.id,n&&this.tree.removeNodeFromIndex(e),e.setData(t),n&&this.tree.addNodeToIndex(e),this.renderer.renderNode(e),this._selectCurrentNode()},o.prototype.moveNode=function(e,t,n){var o;return o=c.nameToIndex(n),this.tree.moveNode(e,t,o),this._refreshElements()},o.prototype.getStateFromStorage=function(){return this.save_state_handler.getStateFromStorage()},o.prototype.addToSelection=function(e){return e?(this.select_node_handler.addToSelection(e),this._getNodeElementForNode(e).select(),this._saveState()):void 0},o.prototype.getSelectedNodes=function(){return this.select_node_handler.getSelectedNodes()},o.prototype.isNodeSelected=function(e){return this.select_node_handler.isNodeSelected(e)},o.prototype.removeFromSelection=function(e){return this.select_node_handler.removeFromSelection(e),this._getNodeElementForNode(e).deselect(),this._saveState()},o.prototype.scrollToNode=function(t){var n,o;return n=e(t.element),o=n.offset().top-this.$el.offset().top,this.scroll_handler.scrollTo(o)},o.prototype.getState=function(){return this.save_state_handler.getState()},o.prototype.setState=function(e){return this.save_state_handler.setState(e),this._refreshElements()},o.prototype.setOption=function(e,t){return this.options[e]=t},o.prototype.getVersion=function(){return F},o.prototype._init=function(){return o.__super__._init.call(this),this.element=this.$el,this.mouse_delay=300,this.is_initialized=!1,this.renderer=new r(this),"undefined"!=typeof _&&null!==_?this.save_state_handler=new _(this):this.options.saveState=!1,"undefined"!=typeof g&&null!==g&&(this.select_node_handler=new g(this)),"undefined"!=typeof n&&null!==n?this.dnd_handler=new n(this):this.options.dragAndDrop=!1,"undefined"!=typeof f&&null!==f&&(this.scroll_handler=new f(this)),"undefined"!=typeof u&&null!==u&&"undefined"!=typeof g&&null!==g&&(this.key_handler=new u(this)),this._initData(),this.element.click(e.proxy(this._click,this)),this.element.dblclick(e.proxy(this._dblclick,this)),this.options.useContextMenu?this.element.bind("contextmenu",e.proxy(this._contextmenu,this)):void 0},o.prototype._deinit=function(){return this.element.empty(),this.element.unbind(),this.key_handler.deinit(),this.tree=null,o.__super__._deinit.call(this)},o.prototype._initData=function(){return this.options.data?this._loadData(this.options.data):this._loadDataFromUrl(this._getDataUrlInfo())},o.prototype._getDataUrlInfo=function(t){var n,o;return n=this.options.dataUrl||this.element.data("url"),o=function(e){return function(){var o,r,i;return i={url:n},t&&t.id?(o={node:t.id},i.data=o):(r=e._getNodeIdToBeSelected(),r&&(o={selected_node:r},i.data=o)),i}}(this),e.isFunction(n)?n(t):"string"===e.type(n)?o():n},o.prototype._getNodeIdToBeSelected=function(){return this.options.saveState?this.save_state_handler.getNodeIdToBeSelected():null},o.prototype._initTree=function(e){return this.tree=new this.options.nodeClass(null,!0,this.options.nodeClass),this.select_node_handler&&this.select_node_handler.clear(),this.tree.loadFromData(e),this._openNodes(),this._refreshElements(),this.is_initialized?void 0:(this.is_initialized=!0,this._triggerEvent("tree.init"))},o.prototype._openNodes=function(){var e;if(!(this.options.saveState&&this.save_state_handler.restoreState()||this.options.autoOpen===!1))return e=this.options.autoOpen===!0?-1:parseInt(this.options.autoOpen),this.tree.iterate(function(t,n){return t.hasChildren()&&(t.is_open=!0),n!==e})},o.prototype._refreshElements=function(e){return null==e&&(e=null),this.renderer.render(e),this._triggerEvent("tree.refresh")},o.prototype._click=function(e){var t,n,o;if(t=this._getClickTarget(e.target)){if("button"===t.type)return this.toggle(t.node,this.options.slide),e.preventDefault(),e.stopPropagation();if("label"===t.type&&(o=t.node,n=this._triggerEvent("tree.click",{node:o,click_event:e}),!n.isDefaultPrevented()))return this._selectNode(o,!0)}},o.prototype._dblclick=function(e){var t;return t=this._getClickTarget(e.target),t&&"label"===t.type?this._triggerEvent("tree.dblclick",{node:t.node,click_event:e}):void 0},o.prototype._getClickTarget=function(t){var n,o,r,i;if(r=e(t),n=r.closest(".jqtree-toggler"),n.length){if(i=this._getNode(n))return{type:"button",node:i}}else if(o=r.closest(".jqtree-element"),o.length&&(i=this._getNode(o)))return{type:"label",node:i};return null},o.prototype._getNode=function(e){var t;return t=e.closest("li.jqtree_common"),0===t.length?null:t.data("node")},o.prototype._getNodeElementForNode=function(e){return e.isFolder()?new i(e,this):new p(e,this)},o.prototype._getNodeElement=function(e){var t;return t=this._getNode(e),t?this._getNodeElementForNode(t):null},o.prototype._contextmenu=function(t){var n,o;return n=e(t.target).closest("ul.jqtree-tree .jqtree-element"),n.length&&(o=this._getNode(n))?(t.preventDefault(),t.stopPropagation(),this._triggerEvent("tree.contextmenu",{node:o,click_event:t}),!1):void 0},o.prototype._saveState=function(){return this.options.saveState?this.save_state_handler.saveState():void 0},o.prototype._mouseCapture=function(e){return this.options.dragAndDrop?this.dnd_handler.mouseCapture(e):!1},o.prototype._mouseStart=function(e){return this.options.dragAndDrop?this.dnd_handler.mouseStart(e):!1},o.prototype._mouseDrag=function(e){var t;return this.options.dragAndDrop?(t=this.dnd_handler.mouseDrag(e),this.scroll_handler&&this.scroll_handler.checkScrolling(),t):!1},o.prototype._mouseStop=function(e){return this.options.dragAndDrop?this.dnd_handler.mouseStop(e):!1},o.prototype._triggerEvent=function(t,n){var o;return o=e.Event(t),e.extend(o,n),this.element.trigger(o),o},o.prototype.testGenerateHitAreas=function(e){return this.dnd_handler.current_item=this._getNodeElementForNode(e),this.dnd_handler.generateHitAreas(),this.dnd_handler.hit_areas},o.prototype._selectCurrentNode=function(){var e,t;return e=this.getSelectedNode(),e&&(t=this._getNodeElementForNode(e))?t.select():void 0},o.prototype._deselectCurrentNode=function(){var e;return e=this.getSelectedNode(),e?this.removeFromSelection(e):void 0},o}(a),m.register(l,"tree"),p=function(){function n(e,t){this.init(e,t)}return n.prototype.init=function(t,n){return this.node=t,this.tree_widget=n,t.element||(t.element=this.tree_widget.element),this.$element=e(t.element)},n.prototype.getUl=function(){return this.$element.children("ul:first")},n.prototype.getSpan=function(){return this.$element.children(".jqtree-element").find("span.jqtree-title")},n.prototype.getLi=function(){return this.$element},n.prototype.addDropHint=function(e){return e===c.INSIDE?new t(this.$element):new s(this.node,this.$element,e)},n.prototype.select=function(){return this.getLi().addClass("jqtree-selected")},n.prototype.deselect=function(){return this.getLi().removeClass("jqtree-selected")},n}(),i=function(e){function n(){return n.__super__.constructor.apply(this,arguments)}return T(n,e),n.prototype.open=function(e,t){var n,o;return null==t&&(t=!0),this.node.is_open?void 0:(this.node.is_open=!0,n=this.getButton(),n.removeClass("jqtree-closed"),n.html(""),n.append(this.tree_widget.renderer.opened_icon_element.cloneNode()),o=function(t){return function(){return t.getLi().removeClass("jqtree-closed"),e&&e(),t.tree_widget._triggerEvent("tree.open",{node:t.node})}}(this),t?this.getUl().slideDown("fast",o):(this.getUl().show(),o()))},n.prototype.close=function(e){var t,n;return null==e&&(e=!0),this.node.is_open?(this.node.is_open=!1,t=this.getButton(),t.addClass("jqtree-closed"),t.html(""),t.append(this.tree_widget.renderer.closed_icon_element.cloneNode()),n=function(e){return function(){return e.getLi().addClass("jqtree-closed"),e.tree_widget._triggerEvent("tree.close",{node:e.node})}}(this),e?this.getUl().slideUp("fast",n):(this.getUl().hide(),n())):void 0},n.prototype.getButton=function(){return this.$element.children(".jqtree-element").find("a.jqtree-toggler")},n.prototype.addDropHint=function(e){return this.node.is_open||e!==c.INSIDE?new s(this.node,this.$element,e):new t(this.$element)},n}(p),N=function(e){return(""+e).replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g,"&quot;").replace(/'/g,"&#x27;").replace(/\//g,"&#x2F;")},C=function(e,t){var n,o,r,i;for(n=r=0,i=e.length;i>r;n=++r)if(o=e[n],o===t)return n;return-1},S=function(e,t){return e.indexOf?e.indexOf(t):C(e,t)},this.Tree.indexOf=S,this.Tree._indexOf=C,w=function(e){return"number"==typeof e&&e%1===0},y=function(){var e,t,n,o,r;return e=/[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,t={"\b":"\\b","	":"\\t","\n":"\\n","\f":"\\f","\r":"\\r",'"':'\\"',"\\":"\\\\"},n=function(n){return e.lastIndex=0,e.test(n)?'"'+n.replace(e,function(e){var n;return n=t[e],"string"==typeof n?n:"\\u"+("0000"+e.charCodeAt(0).toString(16)).slice(-4)})+'"':'"'+n+'"'},o=function(e,t){var r,i,s,d,l,u,a;switch(l=t[e],typeof l){case"string":return n(l);case"number":return isFinite(l)?String(l):"null";case"boolean":case"null":return String(l);case"object":if(!l)return"null";if(s=[],"[object Array]"===Object.prototype.toString.apply(l)){for(r=u=0,a=l.length;a>u;r=++u)d=l[r],s[r]=o(r,l)||"null";return 0===s.length?"[]":"["+s.join(",")+"]"}for(i in l)Object.prototype.hasOwnProperty.call(l,i)&&(d=o(i,l),d&&s.push(n(i)+":"+d));return 0===s.length?"{}":"{"+s.join(",")+"}"}},r=function(e){return o("",{"":e})}},this.Tree.get_json_stringify_function=y,(null==this.JSON||null==this.JSON.stringify||"function"!=typeof this.JSON.stringify)&&(null==this.JSON&&(this.JSON={}),this.JSON.stringify=y()),_=function(){function t(e){this.tree_widget=e}return t.prototype.saveState=function(){var t;return t=JSON.stringify(this.getState()),this.tree_widget.options.onSetStateFromStorage?this.tree_widget.options.onSetStateFromStorage(t):this.supportsLocalStorage()?localStorage.setItem(this.getCookieName(),t):e.cookie?(e.cookie.raw=!0,e.cookie(this.getCookieName(),t,{path:"/"})):void 0},t.prototype.restoreState=function(){var e;return e=this.getStateFromStorage(),e?(this.setState(e),!0):!1},t.prototype.getStateFromStorage=function(){var e;return e=this._loadFromStorage(),e?this._parseState(e):null},t.prototype._parseState=function(t){var n;return n=e.parseJSON(t),n&&n.selected_node&&w(n.selected_node)&&(n.selected_node=[n.selected_node]),n},t.prototype._loadFromStorage=function(){return this.tree_widget.options.onGetStateFromStorage?this.tree_widget.options.onGetStateFromStorage():this.supportsLocalStorage()?localStorage.getItem(this.getCookieName()):e.cookie?(e.cookie.raw=!0,e.cookie(this.getCookieName())):null},t.prototype.getState=function(){var e,t;return e=function(e){return function(){var t;return t=[],e.tree_widget.tree.iterate(function(e){return e.is_open&&e.id&&e.hasChildren()&&t.push(e.id),!0}),t}}(this),t=function(e){return function(){var t;return function(){var e,n,o,r;for(o=this.tree_widget.getSelectedNodes(),r=[],e=0,n=o.length;n>e;e++)t=o[e],r.push(t.id);return r}.call(e)}}(this),{open_nodes:e(),selected_node:t()}},t.prototype.setState=function(e){var t,n,o,r,i,s,d;if(e&&(n=e.open_nodes,r=e.selected_node,this.tree_widget.tree.iterate(function(){return function(e){return e.is_open=e.id&&e.hasChildren()&&S(n,e.id)>=0,!0}}(this)),r&&this.tree_widget.select_node_handler)){for(this.tree_widget.select_node_handler.clear(),d=[],i=0,s=r.length;s>i;i++)t=r[i],o=this.tree_widget.getNodeById(t),d.push(o?this.tree_widget.select_node_handler.addToSelection(o):void 0);return d}},t.prototype.getCookieName=function(){return"string"==typeof this.tree_widget.options.saveState?this.tree_widget.options.saveState:"tree"},t.prototype.supportsLocalStorage=function(){var e;return e=function(){var e,t;if("undefined"==typeof localStorage||null===localStorage)return!1;try{t="_storage_test",sessionStorage.setItem(t,!0),sessionStorage.removeItem(t)}catch(n){return e=n,!1}return!0},null==this._supportsLocalStorage&&(this._supportsLocalStorage=e()),this._supportsLocalStorage},t.prototype.getNodeIdToBeSelected=function(){var e;return e=this.getStateFromStorage(),e&&e.selected_node?e.selected_node[0]:null},t}(),g=function(){function e(e){this.tree_widget=e,this.clear()}return e.prototype.getSelectedNode=function(){var e;return e=this.getSelectedNodes(),e.length?e[0]:!1},e.prototype.getSelectedNodes=function(){var e,t,n;if(this.selected_single_node)return[this.selected_single_node];n=[];for(e in this.selected_nodes)t=this.tree_widget.getNodeById(e),t&&n.push(t);return n},e.prototype.getSelectedNodesUnder=function(e){var t,n,o;if(this.selected_single_node)return e.isParentOf(this.selected_single_node)?[this.selected_single_node]:[];o=[];for(t in this.selected_nodes)n=this.tree_widget.getNodeById(t),n&&e.isParentOf(n)&&o.push(n);return o},e.prototype.isNodeSelected=function(e){return e.id?this.selected_nodes[e.id]:this.selected_single_node?this.selected_single_node.element===e.element:!1},e.prototype.clear=function(){return this.selected_nodes={},this.selected_single_node=null},e.prototype.removeFromSelection=function(e,t){if(null==t&&(t=!1),e.id){if(delete this.selected_nodes[e.id],t)return e.iterate(function(t){return function(){return delete t.selected_nodes[e.id],!0}}(this))}else if(this.selected_single_node&&e.element===this.selected_single_node.element)return this.selected_single_node=null},e.prototype.addToSelection=function(e){return e.id?this.selected_nodes[e.id]=!0:this.selected_single_node=e},e}(),n=function(){function t(e){this.tree_widget=e,this.hovered_area=null,this.$ghost=null,this.hit_areas=[],this.is_dragging=!1,this.current_item=null}return t.prototype.mouseCapture=function(t){var n,o;return n=e(t.target),this.mustCaptureElement(n)?this.tree_widget.options.onIsMoveHandle&&!this.tree_widget.options.onIsMoveHandle(n)?null:(o=this.tree_widget._getNodeElement(n),o&&this.tree_widget.options.onCanMove&&(this.tree_widget.options.onCanMove(o.node)||(o=null)),this.current_item=o,null!==this.current_item):null},t.prototype.mouseStart=function(t){var n;return this.refresh(),n=e(t.target).offset(),this.drag_element=new o(this.current_item.node,t.page_x-n.left,t.page_y-n.top,this.tree_widget.element),this.is_dragging=!0,this.current_item.$element.addClass("jqtree-moving"),!0
},t.prototype.mouseDrag=function(e){var t,n;return this.drag_element.move(e.page_x,e.page_y),t=this.findHoveredArea(e.page_x,e.page_y),n=this.canMoveToArea(t),n&&t?(t.node.isFolder()||this.stopOpenFolderTimer(),this.hovered_area!==t&&(this.hovered_area=t,this.mustOpenFolderTimer(t)?this.startOpenFolderTimer(t.node):this.stopOpenFolderTimer(),this.updateDropHint())):(this.removeHover(),this.removeDropHint(),this.stopOpenFolderTimer()),!0},t.prototype.mustCaptureElement=function(e){return!e.is("input,select")},t.prototype.canMoveToArea=function(e){var t;return e?this.tree_widget.options.onCanMoveTo?(t=c.getName(e.position),this.tree_widget.options.onCanMoveTo(this.current_item.node,e.node,t)):!0:!1},t.prototype.mouseStop=function(e){return this.moveItem(e),this.clear(),this.removeHover(),this.removeDropHint(),this.removeHitAreas(),this.current_item&&(this.current_item.$element.removeClass("jqtree-moving"),this.current_item=null),this.is_dragging=!1,!1},t.prototype.refresh=function(){return this.removeHitAreas(),this.current_item&&(this.generateHitAreas(),this.current_item=this.tree_widget._getNodeElementForNode(this.current_item.node),this.is_dragging)?this.current_item.$element.addClass("jqtree-moving"):void 0},t.prototype.removeHitAreas=function(){return this.hit_areas=[]},t.prototype.clear=function(){return this.drag_element.remove(),this.drag_element=null},t.prototype.removeDropHint=function(){return this.previous_ghost?this.previous_ghost.remove():void 0},t.prototype.removeHover=function(){return this.hovered_area=null},t.prototype.generateHitAreas=function(){var e;return e=new d(this.tree_widget.tree,this.current_item.node,this.getTreeDimensions().bottom),this.hit_areas=e.generate()},t.prototype.findHoveredArea=function(e,t){var n,o,r,i,s;if(o=this.getTreeDimensions(),e<o.left||t<o.top||e>o.right||t>o.bottom)return null;for(i=0,r=this.hit_areas.length;r>i;)if(s=i+r>>1,n=this.hit_areas[s],t<n.top)r=s;else{if(!(t>n.bottom))return n;i=s+1}return null},t.prototype.mustOpenFolderTimer=function(e){var t;return t=e.node,t.isFolder()&&!t.is_open&&e.position===c.INSIDE},t.prototype.updateDropHint=function(){var e;if(this.hovered_area)return this.removeDropHint(),e=this.tree_widget._getNodeElementForNode(this.hovered_area.node),this.previous_ghost=e.addDropHint(this.hovered_area.position)},t.prototype.startOpenFolderTimer=function(e){var t;return t=function(t){return function(){return t.tree_widget._openNode(e,t.tree_widget.options.slide,function(){return t.refresh(),t.updateDropHint()})}}(this),this.stopOpenFolderTimer(),this.open_folder_timer=setTimeout(t,this.tree_widget.options.openFolderDelay)},t.prototype.stopOpenFolderTimer=function(){return this.open_folder_timer?(clearTimeout(this.open_folder_timer),this.open_folder_timer=null):void 0},t.prototype.moveItem=function(e){var t,n,o,r,i,s;return this.hovered_area&&this.hovered_area.position!==c.NONE&&this.canMoveToArea(this.hovered_area)&&(o=this.current_item.node,s=this.hovered_area.node,r=this.hovered_area.position,i=o.parent,r===c.INSIDE&&(this.hovered_area.node.is_open=!0),t=function(e){return function(){return e.tree_widget.tree.moveNode(o,s,r),e.tree_widget.element.empty(),e.tree_widget._refreshElements()}}(this),n=this.tree_widget._triggerEvent("tree.move",{move_info:{moved_node:o,target_node:s,position:c.getName(r),previous_parent:i,do_move:t,original_event:e.original_event}}),!n.isDefaultPrevented())?t():void 0},t.prototype.getTreeDimensions=function(){var e;return e=this.tree_widget.element.offset(),{left:e.left,top:e.top,right:e.left+this.tree_widget.element.width(),bottom:e.top+this.tree_widget.element.height()+16}},t}(),v=function(){function t(e){this.tree=e}return t.prototype.iterate=function(){var t,n;return t=!0,(n=function(o){return function(r,i){var s,d,l,u,a,h,p,c;if(a=(r.is_open||!r.element)&&r.hasChildren(),r.element){if(s=e(r.element),!s.is(":visible"))return;t&&(o.handleFirstNode(r,s),t=!1),r.hasChildren()?r.is_open?o.handleOpenFolder(r,s)||(a=!1):o.handleClosedFolder(r,i,s):o.handleNode(r,i,s)}if(a){for(l=r.children.length,c=r.children,u=h=0,p=c.length;p>h;u=++h)d=c[u],u===l-1?n(r.children[u],null):n(r.children[u],r.children[u+1]);if(r.is_open)return o.handleAfterOpenFolder(r,i,s)}}}(this))(this.tree,null)},t.prototype.handleNode=function(){},t.prototype.handleOpenFolder=function(){},t.prototype.handleClosedFolder=function(){},t.prototype.handleAfterOpenFolder=function(){},t.prototype.handleFirstNode=function(){},t}(),d=function(t){function n(e,t,o){n.__super__.constructor.call(this,e),this.current_node=t,this.tree_bottom=o}return T(n,t),n.prototype.generate=function(){return this.positions=[],this.last_top=0,this.iterate(),this.generateHitAreas(this.positions)},n.prototype.getTop=function(e){return e.offset().top},n.prototype.addPosition=function(e,t,n){var o;return o={top:n,node:e,position:t},this.positions.push(o),this.last_top=n},n.prototype.handleNode=function(e,t,n){var o;return o=this.getTop(n),e===this.current_node?this.addPosition(e,c.NONE,o):this.addPosition(e,c.INSIDE,o),t===this.current_node||e===this.current_node?this.addPosition(e,c.NONE,o):this.addPosition(e,c.AFTER,o)},n.prototype.handleOpenFolder=function(e,t){return e===this.current_node?!1:(e.children[0]!==this.current_node&&this.addPosition(e,c.INSIDE,this.getTop(t)),!0)},n.prototype.handleClosedFolder=function(e,t,n){var o;return o=this.getTop(n),e===this.current_node?this.addPosition(e,c.NONE,o):(this.addPosition(e,c.INSIDE,o),t!==this.current_node?this.addPosition(e,c.AFTER,o):void 0)},n.prototype.handleFirstNode=function(t){return t!==this.current_node?this.addPosition(t,c.BEFORE,this.getTop(e(t.element))):void 0},n.prototype.handleAfterOpenFolder=function(e,t){return e===this.current_node.node||t===this.current_node.node?this.addPosition(e,c.NONE,this.last_top):this.addPosition(e,c.AFTER,this.last_top)},n.prototype.generateHitAreas=function(e){var t,n,o,r,i,s;for(r=-1,t=[],n=[],i=0,s=e.length;s>i;i++)o=e[i],o.top!==r&&t.length&&(t.length&&this.generateHitAreasForGroup(n,t,r,o.top),r=o.top,t=[]),t.push(o);return this.generateHitAreasForGroup(n,t,r,this.tree_bottom),n},n.prototype.generateHitAreasForGroup=function(e,t,n,o){var r,i,s,d,l;for(l=Math.min(t.length,4),r=Math.round((o-n)/l),i=n,s=0;l>s;)d=t[s],e.push({top:i,bottom:i+r,node:d.node,position:d.position}),i+=r,s+=1;return null},n}(v),o=function(){function t(t,n,o,r){this.offset_x=n,this.offset_y=o,this.$element=e('<span class="jqtree-title jqtree-dragging">'+t.name+"</span>"),this.$element.css("position","absolute"),r.append(this.$element)}return t.prototype.move=function(e,t){return this.$element.offset({left:e-this.offset_x,top:t-this.offset_y})},t.prototype.remove=function(){return this.$element.remove()},t}(),s=function(){function t(t,n,o){this.$element=n,this.node=t,this.$ghost=e('<li class="jqtree_common jqtree-ghost"><span class="jqtree_common jqtree-circle"></span><span class="jqtree_common jqtree-line"></span></li>'),o===c.AFTER?this.moveAfter():o===c.BEFORE?this.moveBefore():o===c.INSIDE&&(t.isFolder()&&t.is_open?this.moveInsideOpenFolder():this.moveInside())}return t.prototype.remove=function(){return this.$ghost.remove()},t.prototype.moveAfter=function(){return this.$element.after(this.$ghost)},t.prototype.moveBefore=function(){return this.$element.before(this.$ghost)},t.prototype.moveInsideOpenFolder=function(){return e(this.node.children[0].element).before(this.$ghost)},t.prototype.moveInside=function(){return this.$element.after(this.$ghost),this.$ghost.addClass("jqtree-inside")},t}(),t=function(){function t(t){var n,o;n=t.children(".jqtree-element"),o=t.width()-4,this.$hint=e('<span class="jqtree-border"></span>'),n.append(this.$hint),this.$hint.css({width:o,height:n.height()-4})}return t.prototype.remove=function(){return this.$hint.remove()},t}(),f=function(){function t(e){this.tree_widget=e,this.previous_top=-1,this._initScrollParent()}return t.prototype._initScrollParent=function(){var t,n,o;return n=function(t){return function(){var n,o,r,i,s,d;if(n=["overflow","overflow-y"],(r=function(t){var o,r,i,s;for(r=0,i=n.length;i>r;r++)if(o=n[r],"auto"===(s=e.css(t,o))||"scroll"===s)return!0;return!1})(t.tree_widget.$el[0]))return t.tree_widget.$el;for(d=t.tree_widget.$el.parents(),i=0,s=d.length;s>i;i++)if(o=d[i],r(o))return e(o);return null}}(this),o=function(e){return function(){return e.scroll_parent_top=0,e.$scroll_parent=null}}(this),"fixed"===this.tree_widget.$el.css("position")&&o(),t=n(),t&&t.length&&"HTML"!==t[0].tagName?(this.$scroll_parent=t,this.scroll_parent_top=this.$scroll_parent.offset().top):o()},t.prototype.checkScrolling=function(){var e;return e=this.tree_widget.dnd_handler.hovered_area,e&&e.top!==this.previous_top?(this.previous_top=e.top,this.$scroll_parent?this._handleScrollingWithScrollParent(e):this._handleScrollingWithDocument(e)):void 0},t.prototype._handleScrollingWithScrollParent=function(e){var t;return t=this.scroll_parent_top+this.$scroll_parent[0].offsetHeight-e.bottom,20>t?(this.$scroll_parent[0].scrollTop+=20,this.tree_widget.refreshHitAreas(),this.previous_top=-1):e.top-this.scroll_parent_top<20?(this.$scroll_parent[0].scrollTop-=20,this.tree_widget.refreshHitAreas(),this.previous_top=-1):void 0},t.prototype._handleScrollingWithDocument=function(t){var n;return n=t.top-e(document).scrollTop(),20>n?e(document).scrollTop(e(document).scrollTop()-20):e(window).height()-(t.bottom-e(document).scrollTop())<20?e(document).scrollTop(e(document).scrollTop()+20):void 0},t.prototype.scrollTo=function(t){var n;return this.$scroll_parent?this.$scroll_parent[0].scrollTop=t:(n=this.tree_widget.$el.offset().top,e(document).scrollTop(t+n))},t.prototype.isScrolledIntoView=function(t){var n,o,r,i,s;return n=e(t),this.$scroll_parent?(s=0,i=this.$scroll_parent.height(),r=n.offset().top-this.scroll_parent_top,o=r+n.height()):(s=e(window).scrollTop(),i=s+e(window).height(),r=n.offset().top,o=r+n.height()),i>=o&&r>=s},t}(),u=function(){function t(t){this.tree_widget=t,t.options.keyboardSupport&&e(document).bind("keydown.jqtree",e.proxy(this.handleKeyDown,this))}var n,o,r,i;return o=37,i=38,r=39,n=40,t.prototype.deinit=function(){return e(document).unbind("keydown.jqtree")},t.prototype.handleKeyDown=function(t){var s,d,l,u,a,h,p;if(this.tree_widget.options.keyboardSupport){if(e(document.activeElement).is("textarea,input,select"))return!0;if(s=this.tree_widget.getSelectedNode(),p=function(t){return function(n){return n?(t.tree_widget.selectNode(n),t.tree_widget.scroll_handler&&!t.tree_widget.scroll_handler.isScrolledIntoView(e(n.element).find(".jqtree-element"))&&t.tree_widget.scrollToNode(n),!1):!0}}(this),l=function(e){return function(){return p(e.getNextNode(s))}}(this),h=function(e){return function(){return p(e.getPreviousNode(s))}}(this),a=function(e){return function(){return s.isFolder()&&!s.is_open?(e.tree_widget.openNode(s),!1):!0}}(this),u=function(e){return function(){return s.isFolder()&&s.is_open?(e.tree_widget.closeNode(s),!1):!0}}(this),!s)return!0;switch(d=t.which){case n:return l();case i:return h();case r:return a();case o:return u()}}},t.prototype.getNextNode=function(e,t){var n;return null==t&&(t=!0),t&&e.hasChildren()&&e.is_open?e.children[0]:e.parent?(n=e.getNextSibling(),n?n:this.getNextNode(e.parent,!1)):null},t.prototype.getPreviousNode=function(e){var t;return e.parent?(t=e.getPreviousSibling(),t?t.hasChildren()&&t.is_open?this.getLastChild(t):t:e.parent.parent?e.parent:null):null},t.prototype.getLastChild=function(e){var t;return e.hasChildren()?(t=e.children[e.children.length-1],t.hasChildren()&&t.is_open?this.getLastChild(t):t):null},t}()}).call(this);

var divisionSubmitButton,
    divisionDeleteButton,
    adminSelectize,
    divisionFormBootstrapValidator,
    divisionTree,
    clubName;

function resetDivisionForm(doNotHideDeleteButton) {
    $('#name').val('');
    $('#description').val('');
    adminSelectize[0].selectize.clear();
    $('#oldName').val('');

    //Reset heading
    $('#initDivisionHeading').removeClass('hidden');
    $('#newDivisionHeading').addClass('hidden');
    $('#oldDivisionHeading').addClass('hidden');
    $('#oldDivisionHeadingName').empty();

    //Re-enable form
    divisionFormBootstrapValidator.find('input').prop("disabled", false);
    adminSelectize[0].selectize.enable();
    divisionSubmitButton.enable();
    divisionDeleteButton.enable();

    if(!doNotHideDeleteButton) {
        //Hide delete button
        $('#divisionDelete').addClass('hidden');
    }

    //Reseting previous validation annotation
    divisionFormBootstrapValidator.data('bootstrapValidator').resetForm();
}

function disableDivisionForm() {
    divisionFormBootstrapValidator.find('input').prop("disabled", true);
    adminSelectize[0].selectize.disable();
    divisionSubmitButton.disable();
    divisionDeleteButton.disable();
}

//Loading division information into the form
function loadDivision(name, newDivision) {
    resetDivisionForm();
    divisionSubmitButton.startAnimation();
    //Sending JSON request with the division name as parameter to get the division details
    $.ajax({
        url: '/division',
        type: 'GET',
        data: {
            name: name
        },
        error: function () {
            divisionSubmitButton.stopAnimation(-1, function(button){
                button.disable();
            });
            disableDivisionForm();
        },
        success: function (response) {
            resetDivisionForm();

            $('#oldName').val(response.name);

            var name = $('#name');
            name.focus();

            if(newDivision)
            {
                $('#newDivisionHeading').removeClass('hidden');
            } else
            {
                name.val(response.name);
                $('#description').val(response.desc);
                $('#oldDivisionHeading').removeClass('hidden');
                $('#oldDivisionHeadingName').text('<' + response.name + '>');

                if (response.adminUser) {
                    adminSelectize[0].selectize.addItem(response.adminUser.email);
                }
            }

            $('#initDivisionHeading').addClass('hidden');
            $('#divisionDelete').removeClass('hidden');

            divisionSubmitButton.stopAnimation(1);
        }
    });
}

function loadTree() {
    $('#division-tree-loading').addClass('heartbeat');
    //Loading tree through ajax
    divisionTree.tree(
        'loadDataFromUrl',
        '/division/divisionTree', //URL
        null, //Replace existing tree
        function() {
            $('#division-tree-loading').removeClass('heartbeat'); //Stop loading animation when successful
        }
    );
}

//This function is called as soon as the tab is shown. If necessary it is loading all required resources.
function loadDivisionPage() {

    //Getting the club name, to compare it to the tree nodes. The node with the club name is not allowed to be selected.
    $.ajax({
        url: '/content/clubName',
        type: 'GET',
        error: function () {
            showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
        },
        success: function (data) {
            clubName = data;
        }
    });

    if (!(divisionTree = $('#division-tree')).data().simple_widget_tree) {
        //Configure division tree
        divisionTree.tree({
            autoOpen: true,
            dragAndDrop: true,
            onCanMove: function (node) {
                //Not allowed to move a root node
                return node.parent.parent;
            },
            onCanMoveTo: function (moved_node, target_node, position) {
                //User is not allowed to move a node to a new root position
                return !(!target_node.parent.parent && (position == 'before' || position == 'after'));
            },
            onLoadFailed: function (response) {
                showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                $('#division-tree-loading').removeClass('heartbeat');
            },
            onCanSelectNode: function(node) {
                return node.name != clubName;
            }
        });

        //Clicking on a tree node needs to fill the form
        divisionTree.bind(
            'tree.click',
            function (event) {
                if(event.node.name != clubName) {
                    loadDivision(event.node.name);
                }
            }
        );

        divisionTree.bind(
            'tree.move',
            function (event) {
                $.ajax({
                    url: '/division/divisionTree',
                    type: 'POST',
                    data: {
                        moved_node: event.move_info.moved_node.name,
                        target_node: event.move_info.target_node.name,
                        position: event.move_info.position,
                        previous_parent: event.move_info.previous_parent.name
                    },
                    error: function (response) {
                        showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                        loadTree()
                    },
                    success: function (response) {
                        //console.log(response);
                    }
                });
            }
        );
    }

    if (!(adminSelectize = $('#admin'))[0].selectize) {
        //Enabling selection of admin userfrom existing user
        adminSelectize.selectize({
            persist: false,
            createOnBlur: true,
            create: false, //Not allowing the creation of user specific items
            hideSelected: true, //If an option is allready in the list it is hidden
            preload: true, //Loading data immidiately (if division is loaded without loading the available user, the added user gets removed because selectize thinks he is not valid)
            valueField: 'email',
            labelField: 'email',
            searchField: 'email',
            disable: true,
            maxItems: 1,
            render: {
                option: function (item, escape) {
                    return '<div>' +
                        '<span class="name">' + escape(item.firstName) + ' ' + escape(item.lastName) + ' </span>' +
                        '<span class="description">(' + escape(item.email) + ')</span>' +
                        '</div>';
                }
            },
            load: function (query, callback) {
                $.ajax({
                    url: '/user',
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
    } else
    {
        //Update entries within selectize list
        adminSelectize[0].selectize.load(function (callback) {
            $.ajax({
                url: '/user',
                type: 'GET',
                error: function () {
                    callback();
                },
                success: function (data) {
                    callback(data);
                }
            });
        });
    }

    if (!(divisionFormBootstrapValidator = $('#divisionForm')).data('bootstrapValidator')) {
        //Enable bootstrap validator
        divisionFormBootstrapValidator.bootstrapValidator() //The constrains are configured within the HTML
            .on('success.form.bv', function (e) { //The submit function
                // Prevent form submission
                e.preventDefault();
                //Starting button animation
                divisionSubmitButton.startAnimation();
                //Send the serialized form
                $.ajax({
                    url: '/division',
                    type: 'POST',
                    data: $(e.target).serialize(),
                    error: function (response) {
                        divisionSubmitButton.stopAnimation(-1);
                        showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                    },
                    success: function (response) {
                        divisionSubmitButton.stopAnimation(1);
                        showMessage(response, 'success', 'icon_check');
                        $('#oldName').val($('#name').val()); //If the name changed and the division is not reloaded the oldName needs to be resetted
                        loadTree();
                    }
                });
            });
    }

    if (!divisionSubmitButton) {
        //Enabling progress button
        divisionSubmitButton = new UIProgressButton(document.getElementById('divisionSubmitButton'));
    }

    if(!divisionDeleteButton) {
        //Enabling progress button
        divisionDeleteButton = new UIProgressButton(document.getElementById('divisionDelete'));
        $('#divisionDeleteButton').click(function(e){
            e.preventDefault();
            divisionDeleteButton.startAnimation();
            $.ajax({
                url: '/division?divisionName=' + $('#oldName').val(), //Workaround since DELETE request needs to be identified by the URI only and jQuery is not attaching the data to the URI, which leads to a Spring error.
                type: 'DELETE',
                //data: {
                //    divisionName: $('#oldName').val()
                //},
                error: function (response) {
                    divisionDeleteButton.stopAnimation(-1);
                    showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                },
                success: function (response) {
                    divisionDeleteButton.stopAnimation(1, function(button){
                        classie.add(button.el, 'hidden');
                    });
                    showMessage(response, 'success', 'icon_check');
                    resetDivisionForm(true);
                    disableDivisionForm();
                    loadTree();
                }
            });
        })
    }

    $('#addDivision').click(function(e){
        divisionSubmitButton.startAnimation();
        $.ajax({
            url: '/division',
            type: 'POST',
            data: {
                'new': true
            },
            error: function(response) {
                loadTree();
                resetDivisionForm();
                divisionSubmitButton.stopAnimation(-1);
                showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
            },
            success: function(response) {
                divisionSubmitButton.stopAnimation(1);
                //Separating response message and name of the new division
                var splitResponse = response.split("||");
                loadDivision(splitResponse[1], true);
                loadTree();
                showMessage(splitResponse[0], 'success', 'icon_check');
            }
        });
    });

    loadTree();
    resetDivisionForm();
    disableDivisionForm();
}