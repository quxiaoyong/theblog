﻿jQuery&&function(b){b.extend(b.fn,{fileTree:function(a,d){a||(a={});void 0==a.root&&(a.root="/");void 0==a.script&&(a.script="jqueryFileTree.php");void 0==a.folderEvent&&(a.folderEvent="click");void 0==a.expandSpeed&&(a.expandSpeed=500);void 0==a.collapseSpeed&&(a.collapseSpeed=500);void 0==a.expandEasing&&(a.expandEasing=null);void 0==a.collapseEasing&&(a.collapseEasing=null);void 0==a.multiFolder&&(a.multiFolder=!0);void 0==a.loadMessage&&(a.loadMessage="Loading...");b(this).each(function(){function e(c,
d){b(c).addClass("wait");b(".jqueryFileTree.start").remove();b.post(a.script,{dir:d},function(e){b(c).find(".start").html("");b(c).removeClass("wait").append(e);a.root==d?b(c).find("UL:hidden").show():b(c).find("UL:hidden").slideDown({duration:a.expandSpeed,easing:a.expandEasing});f(c)})}function f(c){b(c).find("LI A").bind(a.folderEvent,function(){b(this).parent().hasClass("directory")?(b(this).parent().hasClass("collapsed")?(a.multiFolder||(b(this).parent().parent().find("UL").slideUp({duration:a.collapseSpeed,
easing:a.collapseEasing}),b(this).parent().parent().find("LI.directory").removeClass("expanded").addClass("collapsed")),b(this).parent().find("UL").remove(),e(b(this).parent(),escape(b(this).attr("rel").match(/.*\//))),b(this).parent().removeClass("collapsed").addClass("expanded")):(b(this).parent().find("UL").slideUp({duration:a.collapseSpeed,easing:a.collapseEasing}),b(this).parent().removeClass("expanded").addClass("collapsed")),d(b(this).attr("red"))):d(b(this).attr("rel"));return!1});"click"!=
a.folderEvent.toLowerCase&&b(c).find("LI A").bind("click",function(){return!1})}b(this).html('\x3cul class\x3d"jqueryFileTree start"\x3e\x3cli class\x3d"wait"\x3e'+a.loadMessage+"\x3cli\x3e\x3c/ul\x3e");e(b(this),escape(a.root))})}})}(jQuery);