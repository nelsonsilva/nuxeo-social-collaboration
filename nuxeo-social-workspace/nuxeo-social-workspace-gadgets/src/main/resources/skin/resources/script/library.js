var Library={};

var prefs = new gadgets.Prefs();
// load the page that will display the content of document specified
Library.documentList = function (docRef, page){
  data = Library.loadContext();

  if ( typeof page == 'number' ) {
    data.page = page;
  }
  data.limit = prefs.getString("limit");

  // set new value of docRef
  data.docRef = docRef;

  Library.loadContent(Library.getBasePath() + '/' + "documentList", data);
}

Library.confirmDeleteDocument = function (targetRef, targetTitle){
  message = prefs.getMsg("label.gadget.library.delete")+' "' +  targetTitle + '"'+ prefs.getMsg("label.gadget.library.interrogation.mark");
  code = 'deleteDocument( \'' + targetRef + '\' );' ;
  Library.showConfirmationPopup(message, code);
}

// delete specified document from repository
Library.deleteDocument = function (targetRef){
  data = Library.loadContext();
  data.targetRef = targetRef;
  Library.loadContent(Library.getBasePath() + '/' + "deleteDocument", data);
}

Library.confirmPublishDocument = function (targetRef, targetTitle, public){
  if ( public ) {
    message = prefs.getMsg("label.gadget.library.make.public.begining")+' "' +  targetTitle + '" '+prefs.getMsg("label.gadget.library.make.public.end");
  } else {
    message = prefs.getMsg("label.gadget.library.make.restricted.begining")+' "' +  targetTitle + '" '+prefs.getMsg("label.gadget.library.make.restricted.end");
  }
  code = 'publishDocument( \'' + targetRef + '\', ' + public + ' );' ;
  Library.showConfirmationPopup(message, code);
}

// publish targetDocument
Library.publishDocument = function (targetRef, public){
  data = Library.loadContext();
  data.targetRef = targetRef;
  if ( typeof public != 'undefined' ) {
    data.public = public;
  }
  Library.loadContent(Library.getBasePath() + '/' + "publishDocument", data);
}

Library.goToDocument = function (path, viewId) {
   window.parent.location = top.nxContextPath + "/nxpath/" + ContextManagement.getTargetRepository() + Library.encode(path) + "@" + viewId;
}

Library.encode = function (path) {
  var segments = path.split('/');
  for (var i = 0; i < segments.length; i++) {
    segments[i] = encodeURIComponent(segments[i]);
  }
  return segments.join('/');
}

// load navigation info from context form
Library.loadContext = function () {
    context = {};
  jQuery.each(jQuery('[name="contextInfoForm"]').serializeArray(), function(i, field){
    context[field.name]=field.value;
  });
  return context;
}

Library.loadContent = function (path, data) {
  // add language
  data.lang = prefs.getLang();

  jQuery.post(
    path,
    data,
    Library.contentLoadedHandler
  );
}

// called when iframe  is loaded ; used for multipart forms ( see create_document_form.ftl)
Library.iframeLoaded = function (iframe) {
	text=jQuery(iframe).contents().find('body').html();
	if ( !Library.isEmpty(text) ) {
		jQuery.fancybox.close();
		Library.contentLoadedHandler(text);
	}
}

//
Library.contentLoadedHandler = function (data){
  // set the new content in "content" element
  jQuery("#content").html(data);

  Library.addPopupBoxTo(jQuery(".addPopup"));

  // intercept forms submit event and add custom behavior
  jQuery("form").submit(
    function(event){
      event.preventDefault();
      data = jQuery(this).serializeArray();
      data.push({
        name: 'limit' ,
        value : prefs.getString("limit")
      });
      Library.loadContent(jQuery(this).attr("action"),data);
    }
  );

  // add the language parameter to all links
  l = prefs.getLang();
  jQuery("a").attr('href', function(i, h) {
    if ( typeof h != 'undefined' ) {
  	  if ( h.indexOf("javascript") == 0 )  { // don't add language to href starting with javascript
  	    return h;
  	  } else {
     	return h + (h.indexOf('?') != -1 ? "&lang=" : "?lang=") + l;
      }
    }
  });

  // remove "alt" attribute from images to avoid be displayed in IE
  if ( jQuery.browser.msie ) {
  	jQuery("img").removeAttr("alt");
  }

  gadgets.window.adjustHeight();
}

// return the path to access social webengine module
Library.getBasePath = function () {
  return basePath = top.nxContextPath + '/site/social';
}

// display an confirmation dialog
// message - message that will be displayed
// code - code that will be executed(as string) if ok button is pressed
Library.showConfirmationPopup = function (message, code ) {
  t = '<div class="fancyContent">';
  t += '<div class="fancyMessage">' + message + '</div>';
  t += '<div class="center">';
  t += '<button class="border" name="ok" type="button" onclick="jQuery.fancybox.close();'+ code +'">OK</button>';
  t += '<button class="border" name="cancel" type="button" onclick="jQuery.fancybox.close()">Cancel</button>';
  t += '</div>';
  t += '</div>';
  jQuery.fancybox(
    t,
    {
      'showCloseButton'  : false,
      'autoDimensions'  : false,
      'width'           : '94%',
      'height'          : '94%',
      'padding'         : 0,
      'transitionIn'    : 'none',
      'transitionOut'    : 'none'
    }
  );
}

Library.addPopupBoxTo = function (a) {
      jQuery(a).fancybox({
        'width'             : '100%',
        'height'            : '100%',
        'autoScale'         : false,
        'transitionIn'      : 'none',
        'transitionOut'     : 'none',
        'type'              : 'iframe',
        'enableEscapeButton': true,
        'centerOnScroll'  : true,
        'showCloseButton'  : false,
        'padding'      : 0,
        'margin'      : 0,
        'overlayShow'    : false
      });
}

// called when gadget is load first time
Library.loadInitialContent = function () {
    Library.documentList(getTargetContextPath());
}

Library.isEmpty = function (s) {
    return (!s || s.length === 0 );
}

gadgets.util.registerOnLoadHandler(Library.loadInitialContent);
