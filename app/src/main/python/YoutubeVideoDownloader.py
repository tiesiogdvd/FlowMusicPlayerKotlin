import yt_dlp
import os

import Presets


def dictFilter(dictionary: dict, keysToKeep: list):
    if keysToKeep is None:
        keysTokeep = [
            'title', 'playlist_index', 'album',
            'artist', 'release_date', 'playlist',
            'filepath', 'id'
        ]

    keys = list(dictionary.keys())

    for key in keys:
        if not (key in keysTokeep):
            dictionary.pop(key)

    return dictionary


videosInfo = list()


saveChapters = False


progress_callback = None


def progressHook(info: dict):
    if info['status'] == 'finished':
        # give progress_callback index of the downloaded item (for playlists)
        pass

    elif info['status'] == 'downloading':
        print(
            info['downloaded_bytes'],
            info['total_bytes'],
            info['speed'],
            info['eta'],
            sep=' | ', end='\n'
        )

        """ progress_callback.invoke(
            int(info['total_bytes']),
            int(info['downloaded_bytes']),
            int(info['speed']),
            int(info['eta'])
        ) """


def getInfo(url: str, callback):
    ydl_opts = {
        'quiet': True,

        'dump_single_json': True,
        'extract_flat': True,
        'ignoreerrors': True,
        'skip_download': True,

        'youtube_include_dash_manifest': False,
        'youtube_include_hls_manifest': False,
    }

    try:
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info: dict = ydl.extract_info(url)
    except yt_dlp.utils.DownloadError as e:
        # Handle the error, for example:
        print(f"Error: {e}")
        return

    #printListableKeysRecursive(info)

    callback(info)


listableTypes = [dict, list]


def printListableKeysRecursive(listable, indentCount=4):
    indentStr = ""

    if indentCount > 0:
        for i in range(0, indentCount, 4):
            indentStr += "----"

    if type(listable) is dict:
        for key in listable.keys():
            print(indentStr, key, type(listable[key]))

            if type(listable[key]) in listableTypes:
                printListableKeysRecursive(listable[key], indentCount + 4)

    elif type(listable) is list:
        for i in range(len(listable)):
            print(indentStr, i, type(listable[i]))

            if type(listable[i]) in listableTypes:
                printListableKeysRecursive(listable[i], indentCount + 4)


def downloadVideo(url: str, ffmpegExecutable: str, callback):
    print(os.environ["HOME"])
    print(ffmpegExecutable)

    callback(1, 1, 1, 1)

    global progress_callback
    progress_callback = callback

    preset = Presets.audioPreset

    ''' def postHook(info: dict):
        if info['status'] == 'finished' and
        info['postprocessor'] == 'MoveFiles':
            if saveChapters == True and
             type(info['info_dict']['chapters']) != 'dict' and
              info['info_dict']['chapters'] != None:
                writeChapters(info['info_dict']['chapters'],
                            info['info_dict']['filepath'])

            videosInfo.append(dictFilter(info['info_dict'])) '''

    ydl_opts = {
        'quiet': False,
        'ignoreerrors': True,

        'restrictfilenames': True,

        'paths': {'home': os.environ["HOME"]},  # save in local app storage

        'writesubtitles': preset.subtitles,
        'subtitleslangs': preset.subtitlesLanguage,
        'subtitlesformat': preset.subtitleFormat,

        'writethumbnail': preset.thumbnails,

        'format': preset.format,
        'outtmpl': {'default': preset.outputTemplate},

        'postprocessors': [],  # post processors can be added later
        'postprocessor_hooks': ["""postHook"""],
        'progress_hooks': [progressHook],

        'ffmpeg_location': ffmpegExecutable
    }

    if preset.extractAudio:
        ydl_opts['postprocessors'].append(
            {
                'key': 'FFmpegExtractAudio',
                'preferredquality': 0
            }
        )

    if preset.subtitles:
        ydl_opts['postprocessors'].append(
            {
                'key': 'FFmpegSubtitlesConvertor',
                'format': 'lrc'
            }
        )

    if preset.outputFileType is not None:
        ydl_opts['merge_output_format'] = preset.outputFileType

    if preset.archive != '':
        ydl_opts['download_archive'] = preset.archive

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        ydl.extract_info(url)

    tagVideos(videosInfo)


def tagVideos(videosInfo):
    pass
