package com.carocean.media.service;

import java.util.HashMap;
import java.util.Locale;

import com.carocean.media.constants.MediaScanConstans.FileType;

import android.annotation.SuppressLint;
import android.mtp.MtpConstants;


public class MediaFile {
    // Audio file types
    private static final int FIRST_AUDIO_FILE_TYPE = FileType.FILE_TYPE_MP3;
    private static final int LAST_AUDIO_FILE_TYPE  = FileType.FILE_TYPE_FLAC;

    // MIDI file types
    private static final int FIRST_MIDI_FILE_TYPE  = FileType.FILE_TYPE_MID;
    private static final int LAST_MIDI_FILE_TYPE  = FileType.FILE_TYPE_IMY;
   
    // Video file types
    private static final int FIRST_VIDEO_FILE_TYPE  = FileType.FILE_TYPE_MP4;
    private static final int LAST_VIDEO_FILE_TYPE  = FileType.FILE_TYPE_WEBM;
    
    // More video file types
    private static final int FIRST_VIDEO_FILE_TYPE2  = FileType.FILE_TYPE_MP2PS;
    private static final int LAST_VIDEO_FILE_TYPE2  = FileType.FILE_TYPE_RM;

    // Image file types
    private static final int FIRST_IMAGE_FILE_TYPE  = FileType.FILE_TYPE_JPEG;
    private static final int LAST_IMAGE_FILE_TYPE  = FileType.FILE_TYPE_WEBP;
   
    // Playlist file types
    private static final int FIRST_PLAYLIST_FILE_TYPE  = FileType.FILE_TYPE_M3U;
    private static final int LAST_PLAYLIST_FILE_TYPE  = FileType.FILE_TYPE_HTTPLIVE;

    // Drm file types
    private static final int FIRST_DRM_FILE_TYPE  = FileType.FILE_TYPE_FL;
    private static final int LAST_DRM_FILE_TYPE  = FileType.FILE_TYPE_FL;

    public static class MediaFileType {
        public final int fileType;
        public final String mimeType;
        
        MediaFileType(int fileType, String mimeType) {
            this.fileType = fileType;
            this.mimeType = mimeType;
        }
    }
    
    private static final HashMap<String, MediaFileType> sFileTypeMap
            = new HashMap<String, MediaFileType>();
    private static final HashMap<String, Integer> sMimeTypeMap
            = new HashMap<String, Integer>();
    // maps file extension to MTP format code
    private static final HashMap<String, Integer> sFileTypeToFormatMap
            = new HashMap<String, Integer>();
    // maps mime type to MTP format code
    private static final HashMap<String, Integer> sMimeTypeToFormatMap
            = new HashMap<String, Integer>();
    // maps MTP format code to mime type
    @SuppressLint("UseSparseArrays")
	private static final HashMap<Integer, String> sFormatToMimeTypeMap
            = new HashMap<Integer, String>();

    static void addFileType(String extension, int fileType, String mimeType) {
        sFileTypeMap.put(extension, new MediaFileType(fileType, mimeType));
        sMimeTypeMap.put(mimeType, Integer.valueOf(fileType));
    }

    static void addFileType(String extension, int fileType, String mimeType, int mtpFormatCode) {
        addFileType(extension, fileType, mimeType);
        sFileTypeToFormatMap.put(extension, Integer.valueOf(mtpFormatCode));
        sMimeTypeToFormatMap.put(mimeType, Integer.valueOf(mtpFormatCode));
        sFormatToMimeTypeMap.put(mtpFormatCode, mimeType);
    }
    static {
    	addFileType("MP1", FileType.FILE_TYPE_MP3, "audio/mpeg");
    	addFileType("MP2", FileType.FILE_TYPE_MP3, "audio/mpeg");
        addFileType("MP3", FileType.FILE_TYPE_MP3, "audio/mpeg", MtpConstants.FORMAT_MP3);
        addFileType("MPGA", FileType.FILE_TYPE_MP3, "audio/mpeg", MtpConstants.FORMAT_MP3);
        addFileType("M4A", FileType.FILE_TYPE_M4A, "audio/mp4", MtpConstants.FORMAT_MPEG);
        addFileType("WAV", FileType.FILE_TYPE_WAV, "audio/x-wav", MtpConstants.FORMAT_WAV);
        addFileType("AMR", FileType.FILE_TYPE_AMR, "audio/amr");
        addFileType("AWB", FileType.FILE_TYPE_AWB, "audio/amr-wb");
        addFileType("WMA", FileType.FILE_TYPE_WMA, "audio/x-ms-wma", MtpConstants.FORMAT_WMA);
        addFileType("OGG", FileType.FILE_TYPE_OGG, "audio/ogg", MtpConstants.FORMAT_OGG);
        addFileType("OGG", FileType.FILE_TYPE_OGG, "application/ogg", MtpConstants.FORMAT_OGG);
        addFileType("OGA", FileType.FILE_TYPE_OGG, "application/ogg", MtpConstants.FORMAT_OGG);
        addFileType("AAC", FileType.FILE_TYPE_AAC, "audio/aac", MtpConstants.FORMAT_AAC);
        addFileType("AAC", FileType.FILE_TYPE_AAC, "audio/aac-adts", MtpConstants.FORMAT_AAC);
        addFileType("MKA", FileType.FILE_TYPE_MKA, "audio/x-matroska");
        addFileType("RA", FileType.FILE_TYPE_RA, "audio/x-pn-realaudio");
        addFileType("AIFF", FileType.FILE_TYPE_AIFF, "audio/x-aiff");
        addFileType("AC3", FileType.FILE_TYPE_MP3, "audio/mpeg");
 
        addFileType("MID", FileType.FILE_TYPE_MID, "audio/midi");
        addFileType("MIDI", FileType.FILE_TYPE_MID, "audio/midi");
        addFileType("XMF", FileType.FILE_TYPE_MID, "audio/midi");
        addFileType("RTTTL", FileType.FILE_TYPE_MID, "audio/midi");
        addFileType("SMF", FileType.FILE_TYPE_SMF, "audio/sp-midi");
        addFileType("IMY", FileType.FILE_TYPE_IMY, "audio/imelody");
        addFileType("RTX", FileType.FILE_TYPE_MID, "audio/midi");
        addFileType("OTA", FileType.FILE_TYPE_MID, "audio/midi");
        addFileType("MXMF", FileType.FILE_TYPE_MID, "audio/midi");
        addFileType("APE", FileType.FILE_TYPE_APE, "audio/ape");
        addFileType("PCM", FileType.FILE_TYPE_APE, "audio/x-wav");
        
        addFileType("MPEG", FileType.FILE_TYPE_MP4, "video/mpeg", MtpConstants.FORMAT_MPEG);
        addFileType("MPG", FileType.FILE_TYPE_MP4, "video/mpeg", MtpConstants.FORMAT_MPEG);
//        addFileType("DAT", FileType.FILE_TYPE_MP4, "video/mpeg", MtpConstants.FORMAT_MPEG);
        addFileType("VOB", FileType.FILE_TYPE_MP4, "video/mpeg", MtpConstants.FORMAT_MPEG);
        addFileType("M2V", FileType.FILE_TYPE_MP4, "video/mpeg", MtpConstants.FORMAT_MPEG);
        addFileType("RM", FileType.FILE_TYPE_RM, "video/vnd.rn-realvideo");
        addFileType("RMVB", FileType.FILE_TYPE_RM, "video/vnd.rn-realvideo");
        addFileType("MP4", FileType.FILE_TYPE_MP4, "video/mp4", MtpConstants.FORMAT_MPEG);
        addFileType("MOV", FileType.FILE_TYPE_MP4, "video/mp4", MtpConstants.FORMAT_MPEG);
        addFileType("M4V", FileType.FILE_TYPE_M4V, "video/mp4", MtpConstants.FORMAT_MPEG);
        addFileType("3GP", FileType.FILE_TYPE_3GPP, "video/3gpp",  MtpConstants.FORMAT_3GP_CONTAINER);
        addFileType("3GPP", FileType.FILE_TYPE_3GPP, "video/3gpp", MtpConstants.FORMAT_3GP_CONTAINER);
        addFileType("3G2", FileType.FILE_TYPE_3GPP2, "video/3gpp2", MtpConstants.FORMAT_3GP_CONTAINER);
        addFileType("3GPP2", FileType.FILE_TYPE_3GPP2, "video/3gpp2", MtpConstants.FORMAT_3GP_CONTAINER);
        addFileType("MKV", FileType.FILE_TYPE_MKV, "video/x-matroska");
        addFileType("WEBM", FileType.FILE_TYPE_WEBM, "video/webm");
        addFileType("TS", FileType.FILE_TYPE_MP2TS, "video/mp2ts");
        addFileType("M2TS", FileType.FILE_TYPE_MP2TS, "video/mp2ts");
        addFileType("M2T", FileType.FILE_TYPE_MP2TS, "video/mp2ts");
        addFileType("AVI", FileType.FILE_TYPE_AVI, "video/avi");
        addFileType("XVID", FileType.FILE_TYPE_AVI, "video/avi");
        addFileType("DIVX", FileType.FILE_TYPE_AVI, "video/avi");
        addFileType("WMV", FileType.FILE_TYPE_WMV, "video/x-ms-wmv", MtpConstants.FORMAT_WMV);
        addFileType("WM", FileType.FILE_TYPE_WMV, "video/x-ms-wm");
        addFileType("ASF", FileType.FILE_TYPE_ASF, "video/x-ms-asf");
        addFileType("ASX", FileType.FILE_TYPE_ASF, "video/x-ms-asf");
        addFileType("FLV", FileType.FILE_TYPE_MP4, "video/mpeg");
        addFileType("OGM", FileType.FILE_TYPE_MP4, "video/mpeg");
        addFileType("TP", FileType.FILE_TYPE_MP4, "video/mpeg");

        addFileType("JPG", FileType.FILE_TYPE_JPEG, "image/jpeg", MtpConstants.FORMAT_EXIF_JPEG);
        addFileType("JPEG", FileType.FILE_TYPE_JPEG, "image/jpeg", MtpConstants.FORMAT_EXIF_JPEG);
        addFileType("GIF", FileType.FILE_TYPE_GIF, "image/gif", MtpConstants.FORMAT_GIF);
        addFileType("PNG", FileType.FILE_TYPE_PNG, "image/png", MtpConstants.FORMAT_PNG);
        addFileType("BMP", FileType.FILE_TYPE_BMP, "image/x-ms-bmp", MtpConstants.FORMAT_BMP);
        addFileType("WBMP", FileType.FILE_TYPE_WBMP, "image/vnd.wap.wbmp");
        addFileType("WEBP", FileType.FILE_TYPE_WEBP, "image/webp");
 
        addFileType("M3U", FileType.FILE_TYPE_M3U, "audio/x-mpegurl", MtpConstants.FORMAT_M3U_PLAYLIST);
        addFileType("M3U", FileType.FILE_TYPE_M3U, "application/x-mpegurl", MtpConstants.FORMAT_M3U_PLAYLIST);
        addFileType("PLS", FileType.FILE_TYPE_PLS, "audio/x-scpls", MtpConstants.FORMAT_PLS_PLAYLIST);
        addFileType("WPL", FileType.FILE_TYPE_WPL, "application/vnd.ms-wpl", MtpConstants.FORMAT_WPL_PLAYLIST);
        addFileType("M3U8", FileType.FILE_TYPE_HTTPLIVE, "application/vnd.apple.mpegurl");
        addFileType("M3U8", FileType.FILE_TYPE_HTTPLIVE, "audio/mpegurl");
        addFileType("M3U8", FileType.FILE_TYPE_HTTPLIVE, "audio/x-mpegurl");

        addFileType("FL", FileType.FILE_TYPE_FL, "application/x-android-drm-fl");

        addFileType("TXT", FileType.FILE_TYPE_TEXT, "text/plain", MtpConstants.FORMAT_TEXT);
        addFileType("HTM", FileType.FILE_TYPE_HTML, "text/html", MtpConstants.FORMAT_HTML);
        addFileType("HTML", FileType.FILE_TYPE_HTML, "text/html", MtpConstants.FORMAT_HTML);
        addFileType("PDF", FileType.FILE_TYPE_PDF, "application/pdf");
        addFileType("DOC", FileType.FILE_TYPE_MS_WORD, "application/msword", MtpConstants.FORMAT_MS_WORD_DOCUMENT);
        addFileType("XLS", FileType.FILE_TYPE_MS_EXCEL, "application/vnd.ms-excel", MtpConstants.FORMAT_MS_EXCEL_SPREADSHEET);
        addFileType("PPT", FileType.FILE_TYPE_MS_POWERPOINT, "application/mspowerpoint", MtpConstants.FORMAT_MS_POWERPOINT_PRESENTATION);
        addFileType("FLAC", FileType.FILE_TYPE_FLAC, "audio/flac", MtpConstants.FORMAT_FLAC);
        addFileType("ZIP", FileType.FILE_TYPE_ZIP, "application/zip");
        addFileType("MPG", FileType.FILE_TYPE_MP2PS, "video/mp2p");
        addFileType("MPEG", FileType.FILE_TYPE_MP2PS, "video/mp2p");
    }

    public static boolean isAudioFileType(int fileType) {
        return ((fileType >= FIRST_AUDIO_FILE_TYPE &&
                fileType <= LAST_AUDIO_FILE_TYPE) ||
                (fileType >= FIRST_MIDI_FILE_TYPE &&
                fileType <= LAST_MIDI_FILE_TYPE)) ||
                fileType == FileType.FILE_TYPE_APE;
    }

    public static boolean isVideoFileType(int fileType) {
        return (fileType >= FIRST_VIDEO_FILE_TYPE &&
                fileType <= LAST_VIDEO_FILE_TYPE)
            || (fileType >= FIRST_VIDEO_FILE_TYPE2 &&
                fileType <= LAST_VIDEO_FILE_TYPE2);
    }

    public static boolean isImageFileType(int fileType) {
        return (fileType >= FIRST_IMAGE_FILE_TYPE &&
                fileType <= LAST_IMAGE_FILE_TYPE);
    }

    public static boolean isPlayListFileType(int fileType) {
        return (fileType >= FIRST_PLAYLIST_FILE_TYPE &&
                fileType <= LAST_PLAYLIST_FILE_TYPE);
    }

    public static boolean isDrmFileType(int fileType) {
        return (fileType >= FIRST_DRM_FILE_TYPE &&
                fileType <= LAST_DRM_FILE_TYPE);
    }

    public static MediaFileType getFileType(String path) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot < 0)
            return null;
        return sFileTypeMap.get(path.substring(lastDot + 1).toUpperCase(Locale.ROOT));
    }

    public static boolean isMimeTypeMedia(String mimeType) {
        int fileType = getFileTypeForMimeType(mimeType);
        return isAudioFileType(fileType) || isVideoFileType(fileType)
                || isImageFileType(fileType) || isPlayListFileType(fileType);
    }

    // generates a title based on file name
    public static String getFileTitle(String path) {
        // extract file name after last slash
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0) {
            lastSlash++;
            if (lastSlash < path.length()) {
                path = path.substring(lastSlash);
            }
        }
        // truncate the file extension (if any)
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0) {
            path = path.substring(0, lastDot);
        }
        return path;
    }

    public static int getFileTypeForMimeType(String mimeType) {
        Integer value = sMimeTypeMap.get(mimeType);
        return (value == null ? 0 : value.intValue());
    }

    public static String getMimeTypeForFile(String path) {
    	MediaFileType mediaFileType = getFileType(path);
        return (mediaFileType == null ? null : mediaFileType.mimeType);
    }

    public static int getFormatCode(String fileName, String mimeType) {
        if (mimeType != null) {
            Integer value = sMimeTypeToFormatMap.get(mimeType);
            if (value != null) {
                return value.intValue();
            }
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            String extension = fileName.substring(lastDot + 1).toUpperCase(Locale.ROOT);
            Integer value = sFileTypeToFormatMap.get(extension);
            if (value != null) {
                return value.intValue();
            }
        }
        return MtpConstants.FORMAT_UNDEFINED;
    }

    public static String getMimeTypeForFormatCode(int formatCode) {
        return sFormatToMimeTypeMap.get(formatCode);
    }
}