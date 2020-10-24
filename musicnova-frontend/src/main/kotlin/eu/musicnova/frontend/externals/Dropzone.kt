package eu.musicnova.frontend.externals

import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.File
import org.w3c.xhr.FormData
import org.w3c.xhr.XMLHttpRequest

@JsModule("dropzone")
@JsNonModule
external open class Dropzone {
    constructor(container: String, options: DropzoneOptions = definedExternally)
    constructor(container: HTMLElement, options: DropzoneOptions = definedExternally)
    constructor(container: HTMLElement, options: dynamic)

    open var element: HTMLElement
    open var files: Array<DropzoneFile>
    open var hiddenFileInput: HTMLInputElement
    open var listeners: Array<DropzoneListener>
    open var defaultOptions: DropzoneOptions
    open var options: DropzoneOptions
    open var previewsContainer: HTMLElement
    open var version: String
    open fun enable()
    open fun disable()
    open fun destroy(): Dropzone
    open fun addFile(file: DropzoneFile)
    open fun removeFile(file: DropzoneFile)
    open fun removeAllFiles(cancelIfNecessary: Boolean = definedExternally)
    open fun resizeImage(
        file: DropzoneFile,
        width: Number = definedExternally,
        height: Number = definedExternally,
        resizeMethod: String = definedExternally,
        callback: (args: Any) -> Unit = definedExternally
    )

    open fun processQueue()
    open fun cancelUpload(file: DropzoneFile)
    open fun createThumbnail(
        file: DropzoneFile,
        width: Number = definedExternally,
        height: Number = definedExternally,
        resizeMethod: String = definedExternally,
        fixOrientation: Boolean = definedExternally,
        callback: (args: Any) -> Unit = definedExternally
    ): Any

    open fun displayExistingFile(
        mockFile: DropzoneMockFile,
        imageUrl: String,
        callback: () -> Unit = definedExternally,
        crossOrigin: String /* 'anonymous' | 'use-credentials' */ = definedExternally,
        resizeThumbnail: Boolean = definedExternally
    ): Any

    open fun createThumbnailFromUrl(
        file: DropzoneFile,
        width: Number = definedExternally,
        height: Number = definedExternally,
        resizeMethod: String = definedExternally,
        fixOrientation: Boolean = definedExternally,
        callback: (args: Any) -> Unit = definedExternally,
        crossOrigin: String = definedExternally
    ): Any

    open fun processFiles(files: Array<DropzoneFile>)
    open fun processFile(file: DropzoneFile)
    open fun uploadFile(file: DropzoneFile)
    open fun uploadFiles(files: Array<DropzoneFile>)
    open fun getAcceptedFiles(): Array<DropzoneFile>
    open fun getActiveFiles(): Array<DropzoneFile>
    open fun getAddedFiles(): Array<DropzoneFile>
    open fun getRejectedFiles(): Array<DropzoneFile>
    open fun getQueuedFiles(): Array<DropzoneFile>
    open fun getUploadingFiles(): Array<DropzoneFile>
    open fun accept(file: DropzoneFile, done: (error: dynamic /* String | Error */) -> Unit)
    open fun getFilesWithStatus(status: String): Array<DropzoneFile>
    open fun enqueueFile(file: DropzoneFile)
    open fun enqueueFiles(file: Array<DropzoneFile>)
    open fun createThumbnail(file: DropzoneFile, callback: (args: Any) -> Unit = definedExternally): Any
    open fun createThumbnailFromUrl(
        file: DropzoneFile,
        url: String,
        callback: (args: Any) -> Unit = definedExternally
    ): Any

    open fun on(eventName: String, callback: dynamic): Dropzone
    open fun on(eventName: String, callback: (args: Any) -> Unit): Dropzone
    open fun off(): Dropzone
    open fun off(eventName: String, callback: (args: Any) -> Unit = definedExternally): Dropzone
    open fun emit(eventName: String, vararg args: Any): Dropzone
    open fun on(
        eventName: String /* 'drop' | 'dragstart' | 'dragend' | 'dragenter' | 'dragover' | 'dragleave' | 'paste' */,
        callback: (e: DragEvent) -> Any
    ): Dropzone

    open fun on(eventName: String /* 'reset' | 'queuecomplete' */): Dropzone
    open fun on(
        eventName: String /* 'addedfile' | 'removedfile' | 'processing' | 'canceled' | 'complete' | 'maxfilesexceeded' */,
        callback: (file: DropzoneFile) -> Any
    ): Dropzone

    open fun on(
        eventName: String /* 'addedfiles' | 'processingmultiple' | 'successmultiple' | 'maxfilesreached' */,
        callback: (files: Array<DropzoneFile>) -> Any
    ): Dropzone

    @JsName("on")
    open fun onThumbnail(
        eventName: String /* 'thumbnail' */,
        callback: (file: DropzoneFile, dataUrl: String) -> Any
    ): Dropzone

    @JsName("on")
    open fun onError(
        eventName: String /* 'error' */,
        callback: (file: DropzoneFile, message: dynamic /* String | Error */) -> Any
    ): Dropzone

    open fun on(
        eventName: String /* 'errormultiple' */,
        callback: (files: Array<DropzoneFile>, message: dynamic /* String | Error */) -> Any
    ): Dropzone

    open fun on(
        eventName: String /* 'uploadprogress' */,
        callback: (file: DropzoneFile, progress: Number, bytesSent: Number) -> Any
    ): Dropzone

    open fun on(
        eventName: String /* 'totaluploadprogress' */,
        callback: (totalProgress: Number, totalBytes: Number, totalBytesSent: Number) -> Any
    ): Dropzone

    @JsName("on")
    open fun onSeinding(
        eventName: String /* 'sending' */,
        callback: (file: DropzoneFile, xhr: XMLHttpRequest, formData: FormData) -> Any
    ): Dropzone

    @JsName("on")
    open fun onSendingMultiple(
        eventName: String /* 'sendingmultiple' */,
        callback: (files: Array<DropzoneFile>, xhr: XMLHttpRequest, formData: FormData) -> Any
    ): Dropzone

    @JsName("on")
    open fun onSuccess(
        eventName: String /* 'success' */,
        callback: (file: dynamic, response: dynamic /* Object | String */) -> Any
    ): Dropzone

    @JsName("on")
    open fun onMultiple(
        eventName: String /* 'canceledmultiple' | 'completemultiple' */,
        callback: (file: Array<DropzoneFile>) -> Any
    ): Dropzone

    open fun emit(
        eventName: String /* 'drop' | 'dragstart' | 'dragend' | 'dragenter' | 'dragover' | 'dragleave' | 'paste' */,
        e: DragEvent
    ): Dropzone

    open fun emit(eventName: String /* 'reset' | 'queuecomplete' */): Dropzone
    open fun emit(
        eventName: String /* 'addedfile' | 'removedfile' | 'processing' | 'canceled' | 'complete' | 'maxfilesexceeded' */,
        file: DropzoneFile
    ): Dropzone

    open fun emit(
        eventName: String /* 'addedfiles' | 'processingmultiple' | 'successmultiple' | 'canceledmultiple' | 'completemultiple' | 'maxfilesreached' */,
        files: Array<DropzoneFile>
    ): Dropzone

    open fun emit(eventName: String /* 'thumbnail' | 'error' */, file: DropzoneFile, dataUrl: String): Dropzone
    open fun emit(eventName: String /* 'error' */, file: DropzoneFile, message: Error): Dropzone
    open fun emit(eventName: String /* 'errormultiple' */, files: Array<DropzoneFile>, message: String): Dropzone
    open fun emit(eventName: String /* 'errormultiple' */, files: Array<DropzoneFile>, message: Error): Dropzone
    open fun emit(
        eventName: String /* 'uploadprogress' */,
        file: DropzoneFile,
        progress: Number,
        bytesSent: Number
    ): Dropzone

    open fun emit(
        eventName: String /* 'totaluploadprogress' */,
        totalProgress: Number,
        totalBytes: Number,
        totalBytesSent: Number
    ): Dropzone

    open fun emit(
        eventName: String /* 'sending' */,
        file: DropzoneFile,
        xhr: XMLHttpRequest,
        formData: FormData
    ): Dropzone

    open fun emit(
        eventName: String /* 'sendingmultiple' */,
        files: Array<DropzoneFile>,
        xhr: XMLHttpRequest,
        formData: FormData
    ): Dropzone

    open fun emit(eventName: String /* 'success' */, file: DropzoneFile, response: Any?): Dropzone
    open fun emit(eventName: String /* 'success' */, file: DropzoneFile, response: String?): Dropzone
    interface DropzoneResizeInfo {
        var srcX: Number?
            get() = definedExternally
            set(value) = definedExternally
        var srcY: Number?
            get() = definedExternally
            set(value) = definedExternally
        var trgX: Number?
            get() = definedExternally
            set(value) = definedExternally
        var trgY: Number?
            get() = definedExternally
            set(value) = definedExternally
        var srcWidth: Number?
            get() = definedExternally
            set(value) = definedExternally
        var srcHeight: Number?
            get() = definedExternally
            set(value) = definedExternally
        var trgWidth: Number?
            get() = definedExternally
            set(value) = definedExternally
        var trgHeight: Number?
            get() = definedExternally
            set(value) = definedExternally
    }

    interface DropzoneFileUpload {
        var progress: Number
        var total: Number
        var bytesSent: Number
        var uuid: String
        var totalChunkCount: Number?
            get() = definedExternally
            set(value) = definedExternally
    }

    interface DropzoneFile {
        var dataURL: String?
            get() = definedExternally
            set(value) = definedExternally
        var previewElement: HTMLElement
        var previewTemplate: HTMLElement
        var previewsContainer: HTMLElement
        var status: String
        var accepted: Boolean
        var xhr: XMLHttpRequest?
            get() = definedExternally
            set(value) = definedExternally
        var upload: DropzoneFileUpload?
            get() = definedExternally
            set(value) = definedExternally
    }

    interface DropzoneMockFile {
        var name: String
        var size: Number

        @nativeGetter
        operator fun get(index: String): Any?

        @nativeSetter
        operator fun set(index: String, value: Any)
    }

    interface DropzoneDictFileSizeUnits {
        var tb: String?
            get() = definedExternally
            set(value) = definedExternally
        var gb: String?
            get() = definedExternally
            set(value) = definedExternally
        var mb: String?
            get() = definedExternally
            set(value) = definedExternally
        var kb: String?
            get() = definedExternally
            set(value) = definedExternally
        var b: String?
            get() = definedExternally
            set(value) = definedExternally
    }

    interface `T$33` {
        @nativeGetter
        operator fun get(key: String): String?

        @nativeSetter
        operator fun set(key: String, value: String)
    }

      interface DropzoneOptions {

        var url: dynamic /* ((files: ReadonlyArray<DropzoneFile>) -> String)? | String? */
            get() = definedExternally
            set(value) = definedExternally
        var method: dynamic /* ((files: ReadonlyArray<DropzoneFile>) -> String)? | String? */
            get() = definedExternally
            set(value) = definedExternally
        var withCredentials: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var timeout: Number?
            get() = definedExternally
            set(value) = definedExternally
        var parallelUploads: Number?
            get() = definedExternally
            set(value) = definedExternally
        var uploadMultiple: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var chunking: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var forceChunking: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var chunkSize: Number?
            get() = definedExternally
            set(value) = definedExternally
        var parallelChunkUploads: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var retryChunks: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var retryChunksLimit: Number?
            get() = definedExternally
            set(value) = definedExternally
        var maxFilesize: Number?
            get() = definedExternally
            set(value) = definedExternally
        var paramName: String?
            get() = definedExternally
            set(value) = definedExternally
        var createImageThumbnails: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var maxThumbnailFilesize: Number?
            get() = definedExternally
            set(value) = definedExternally
        var thumbnailWidth: Number?
            get() = definedExternally
            set(value) = definedExternally
        var thumbnailHeight: Number?
            get() = definedExternally
            set(value) = definedExternally
        var thumbnailMethod: String? /* 'contain' | 'crop' */
            get() = definedExternally
            set(value) = definedExternally
        var resizeWidth: Number?
            get() = definedExternally
            set(value) = definedExternally
        var resizeHeight: Number?
            get() = definedExternally
            set(value) = definedExternally
        var resizeMimeType: String?
            get() = definedExternally
            set(value) = definedExternally
        var resizeQuality: Number?
            get() = definedExternally
            set(value) = definedExternally
        var resizeMethod: String? /* 'contain' | 'crop' */
            get() = definedExternally
            set(value) = definedExternally
        var filesizeBase: Number?
            get() = definedExternally
            set(value) = definedExternally
        var maxFiles: Number?
            get() = definedExternally
            set(value) = definedExternally
        var params: Any?
            get() = definedExternally
            set(value) = definedExternally
        var headers: `T$33`?
            get() = definedExternally
            set(value) = definedExternally
        var clickable: dynamic /* Boolean? | String? | HTMLElement? | Array<dynamic /* String | HTMLElement */>? */
            get() = definedExternally
            set(value) = definedExternally
        var ignoreHiddenFiles: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var acceptedFiles: String?
            get() = definedExternally
            set(value) = definedExternally
        val renameFilename: ((name: String) -> String)?
            get() = definedExternally
        var autoProcessQueue: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var autoQueue: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var addRemoveLinks: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        var previewsContainer: dynamic /* Boolean? | String? | HTMLElement? */
            get() = definedExternally
            set(value) = definedExternally
        var hiddenInputContainer: HTMLElement?
            get() = definedExternally
            set(value) = definedExternally
        var capture: String?
            get() = definedExternally
            set(value) = definedExternally
        var dictDefaultMessage: String?
            get() = definedExternally
            set(value) = definedExternally
        var dictFallbackMessage: String?
            get() = definedExternally
            set(value) = definedExternally
        var dictFallbackText: String?
            get() = definedExternally
            set(value) = definedExternally
        var dictFileTooBig: String?
            get() = definedExternally
            set(value) = definedExternally
        var dictInvalidFileType: String?
            get() = definedExternally
            set(value) = definedExternally
        var dictResponseError: String?
            get() = definedExternally
            set(value) = definedExternally
        var dictCancelUpload: String?
            get() = definedExternally
            set(value) = definedExternally
        var dictCancelUploadConfirmation: String?
            get() = definedExternally
            set(value) = definedExternally
        var dictRemoveFile: String?
            get() = definedExternally
            set(value) = definedExternally
        var dictRemoveFileConfirmation: String?
            get() = definedExternally
            set(value) = definedExternally
        var dictMaxFilesExceeded: String?
            get() = definedExternally
            set(value) = definedExternally
        var dictFileSizeUnits: DropzoneDictFileSizeUnits?
            get() = definedExternally
            set(value) = definedExternally
        var dictUploadCanceled: String?
            get() = definedExternally
            set(value) = definedExternally
        val accept: ((file: DropzoneFile, done: (error: dynamic /* String | Error */) -> Unit) -> Unit)?
            get() = definedExternally
        val chunksUploaded: ((file: DropzoneFile, done: (error: dynamic /* String | Error */) -> Unit) -> Unit)?
            get() = definedExternally
        val init: (() -> Unit)?
            get() = definedExternally
        var forceFallback: Boolean?
            get() = definedExternally
            set(value) = definedExternally
        val fallback: (() -> Unit)?
            get() = definedExternally
        val resize: ((file: DropzoneFile, width: Number, height: Number, resizeMethod: String) -> DropzoneResizeInfo)?
            get() = definedExternally
        val drop: ((e: DragEvent) -> Unit)?
            get() = definedExternally
        val dragstart: ((e: DragEvent) -> Unit)?
            get() = definedExternally
        val dragend: ((e: DragEvent) -> Unit)?
            get() = definedExternally
        val dragenter: ((e: DragEvent) -> Unit)?
            get() = definedExternally
        val dragover: ((e: DragEvent) -> Unit)?
            get() = definedExternally
        val dragleave: ((e: DragEvent) -> Unit)?
            get() = definedExternally
        val paste: ((e: DragEvent) -> Unit)?
            get() = definedExternally
        val reset: (() -> Unit)?
            get() = definedExternally
        val addedfile: ((file: DropzoneFile) -> Unit)?
            get() = definedExternally
        val addedfiles: ((files: Array<DropzoneFile>) -> Unit)?
            get() = definedExternally
        val removedfile: ((file: DropzoneFile) -> Unit)?
            get() = definedExternally
        val thumbnail: ((file: DropzoneFile, dataUrl: String) -> Unit)?
            get() = definedExternally
        val processing: ((file: DropzoneFile) -> Unit)?
            get() = definedExternally
        val processingmultiple: ((files: Array<DropzoneFile>) -> Unit)?
            get() = definedExternally
        val uploadprogress: ((file: DropzoneFile, progress: Number, bytesSent: Number) -> Unit)?
            get() = definedExternally
        val totaluploadprogress: ((totalProgress: Number, totalBytes: Number, totalBytesSent: Number) -> Unit)?
            get() = definedExternally
        val sending: ((file: DropzoneFile, xhr: XMLHttpRequest, formData: FormData) -> Unit)?
            get() = definedExternally
        val sendingmultiple: ((files: Array<DropzoneFile>, xhr: XMLHttpRequest, formData: FormData) -> Unit)?
            get() = definedExternally
        val success: ((file: DropzoneFile) -> Unit)?
            get() = definedExternally
        val successmultiple: ((files: Array<DropzoneFile>, responseText: String) -> Unit)?
            get() = definedExternally
        val canceled: ((file: DropzoneFile) -> Unit)?
            get() = definedExternally
        val canceledmultiple: ((file: Array<DropzoneFile>) -> Unit)?
            get() = definedExternally
        val complete: ((file: DropzoneFile) -> Unit)?
            get() = definedExternally
        val completemultiple: ((file: Array<DropzoneFile>) -> Unit)?
            get() = definedExternally
        val maxfilesexceeded: ((file: DropzoneFile) -> Unit)?
            get() = definedExternally
        val maxfilesreached: ((files: Array<DropzoneFile>) -> Unit)?
            get() = definedExternally
        val queuecomplete: (() -> Unit)?
            get() = definedExternally
        val transformFile: ((file: DropzoneFile, done: (file: dynamic /* String | Blob */) -> Unit) -> Unit)?
            get() = definedExternally
        var previewTemplate: String?
            get() = definedExternally
            set(value) = definedExternally
    }

    interface `T$34` {
        @nativeGetter
        operator fun get(key: String): ((e: Event) -> Any)?

        @nativeSetter
        operator fun set(key: String, value: (e: Event) -> Any)
    }

    interface DropzoneListener {
        var element: HTMLElement
        var events: `T$34`
    }

    companion object {
        var autoDiscover: Boolean
        var confirm: (question: String, accepted: () -> Unit, rejected: () -> Unit) -> Unit
        fun createElement(string: String): HTMLElement
        fun isBrowserSupported(): Boolean
        var instances: Array<Dropzone>
        var ADDED: String
        var QUEUED: String
        var ACCEPTED: String
        var UPLOADING: String
        var PROCESSING: String
        var CANCELED: String
        var ERROR: String
        var SUCCESS: String
    }
}
