package org.purplejrank.jdk;

public enum JdkDFAState {
	stream,
	stream$magic,
	stream$magic$version,
	stream$magic$version$contents,
	
	contents,
	contents$content,
	contents$contents,
	contents$contents$content,
	
	content,
	content$object,
	content$blockdata,
	
	object,
	object$newObject,
	object$newClass,
	object$newArray,
	object$newString,
	object$newEnum,
	object$newClassDesc,
	object$prevObject,
	object$nullReference,
	object$exception,
	object$TC_RESET,
	
	newClass,
	newClass$TC_CLASS,
	newClass$TC_CLASS$classDesc,
	newClass$TC_CLASS$classDesc$newHandle,
	
	classDesc,
	classDesc$newClassDesc,
	classDesc$nullReference,
	classDesc$prevObject,
	
	superClassDesc,
	superClassDesc$classDesc,
	
	newClassDesc,
	newClassDesc$TC_CLASSDESC,
	newClassDesc$TC_CLASSDESC$className,
	newClassDesc$TC_CLASSDESC$className$serialVersionUID,
	newClassDesc$TC_CLASSDESC$className$serialVersionUID$newHandle,
	newClassDesc$TC_CLASSDESC$className$serialVersionUID$newHandle$classDescInfo,
	newClassDesc$TC_PROXYCLASSDESC,
	newClassDesc$TC_PROXYCLASSDESC$newHandle,
	newClassDesc$TC_PROXYCLASSDESC$newHandle$proxyClassDescInfo,
	
	classDescInfo,
	classDescInfo$classDescFlags,
	classDescInfo$classDescFlags$fields,
	classDescInfo$classDescFlags$fields$classAnnotation,
	classDescInfo$classDescFlags$fields$classAnnotation$superClassDesc,
	
	className,
	className$utf,
	
	serialVersionUID,
	serialVersionUID$long,
	
	classDescFlags,
	classDescFlags$byte,
	
	proxyClassDescInfo,
	proxyClassDescInfo$count,
	proxyClassDescInfo$count$proxyInterfaceName,
	proxyClassDescInfo$count$proxyInterfaceName$classAnnotation,
	proxyClassDescInfo$count$proxyInterfaceName$classAnnotation$superClassDesc,
	
	proxyInterfaceName,
	proxyInterfaceName$utf,
	
	fields,
	fields$count,
	fields$count$fieldDesc,
	
	fieldDesc,
	fieldDesc$primitiveDesc,
	fieldDesc$objectDesc,
	
	primitiveDesc,
	primitiveDesc$prim_typecode,
	primitiveDesc$prim_typecode$fieldName,
	
	objectDesc,
	objectDesc$obj_typecode,
	objectDesc$obj_typecode$fieldName,
	objectDesc$obj_typecode$fieldName$className1,
	
	fieldName,
	fieldName$utf,
	
	className1,
	className1$object,
	
	classAnnotation,
	classAnnotation$endBlockData,
	classAnnotation$contents,
	classAnnotation$contents$endBlockData,
	
	prim_typecode,

	obj_typecode,
	
	newArray,
	newArray$TC_ARRAY,
	newArray$TC_ARRAY$classDesc,
	newArray$TC_ARRAY$classDesc$newHandle,
	newArray$TC_ARRAY$classDesc$newHandle$size,
	newArray$TC_ARRAY$classDesc$newHandle$size$values,
	
	newObject,
	newObject$TC_OBJECT,
	newObject$TC_OBJECT$classDesc,
	newObject$TC_OBJECT$classDesc$newHandle,
	newObject$TC_OBJECT$classDesc$newHandle$classdata,
	
	classdata,
	classdata$nowrclass,
	classdata$wrclass,
	classdata$wrclass$objectAnnotation,
//	classdata$externalContents,
	classdata$objectAnnotation,
	
	nowrclass,
	nowrclass$values,
	
	wrclass,
	wrclass$nowrclass,
	
	objectAnnotation,
	objectAnnotation$endBlockData,
	objectAnnotation$contents,
	objectAnnotation$contents$endBlockData,
	
	blockdata,
	blockdata$blockdatashort,
	blockdata$blockdatalong,
	
	blockdatashort,
	blockdatashort$TC_BLOCKDATA,
	blockdatashort$TC_BLOCKDATA$ubyte_size,
	blockdatashort$TC_BLOCKDATA$ubyte_size$bytes,
	
	blockdatalong,
	blockdatalong$TC_BLOCKDATALONG,
	blockdatalong$TC_BLOCKDATALONG$int_size,
	blockdatalong$TC_BLOCKDATALONG$int_size$bytes,
	
	endBlockData,
	endBlockData$TC_ENDBLOCKDATA,
	
	newString,
	newString$TC_STRING,
	newString$TC_STRING$newHandle,
	newString$TC_STRING$newHandle$utf,
	newString$TC_LONGSTRING,
	newString$TC_LONGSTRING$newHandle,
	newString$TC_LONGSTRING$newHandle$long_utf,
	
	newEnum,
	newEnum$TC_ENUM,
	newEnum$TC_ENUM$classDesc,
	newEnum$TC_ENUM$classDesc$newHandle,
	newEnum$TC_ENUM$classDesc$newHandle$enumConstantName,
	
	enumConstantName,
	enumConstantName$object,
	
	prevObject,
	prevObject$TC_REFERENCE,
	prevObject$TC_REFERENCE$handle,
	
	nullReference,
	nullReference$TC_NULL,
	
	exception,
	exception$TC_EXCEPTION,
	exception$TC_EXCEPTION$reset,
	exception$TC_EXCEPTION$reset$object,
	exception$TC_EXCEPTION$reset$object$reset,
	
	magic,
	magic$STREAM_MAGIC,
	
	version,
	version$STREAM_VERSION,
	
	values,
	
	newHandle,
	
	reset,
}
