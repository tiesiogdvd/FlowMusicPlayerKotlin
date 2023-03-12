import yt_dlp
import os

import Presets
import Tagger


def dictFilter(dictionary: dict, keysToKeep: list = None):
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


def getInfo(url: str, callback):
    ydl_opts = {
        'quiet': True,

        'dump_single_json': True,
        'extract_flat': True,
        'ignoreerrors': False,
        'skip_download': True,
        'default-search': 'ytsearch',
        'youtube_include_dash_manifest': False,
        'youtube_include_hls_manifest': False,
        'noplaylist': True,
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        try:
            # Try to extract info from the original URL
            info = ydl.extract_info(url)
            callback(info)

        except yt_dlp.utils.DownloadError:
            # Case where URL is not valid
            print("Invalid URL")
            # Searching for the query instead
            info = ydl.extract_info(f'ytsearch10:{url}')
            callback(info)

    #  printListableKeysRecursive(info)
    #  callback(info)


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


progressCallback = None
videoDownloadedCallback = None


def downloadVideo(url: str):
    print(os.environ['HOME'])

    preset = Presets.audioPreset

    def postHook(info: dict):
        if (info['status'] == 'finished' and
                info['postprocessor'] == 'MoveFiles'):
            tagVideo(dictFilter(info['info_dict']))

            videoDownloadedCallback(
              info['info_dict']['id'],
              info['info_dict']['filepath']
            )

    def progressHook(info: dict):
        print(info['info_dict'].keys())

        try:
            if info['status'] == 'finished':
                progressCallback(info['info_dict']['id'], 100.0)
            elif info['status'] == 'downloading':
                percent = float(info['_percent_str'].strip('%'))
                progressCallback(info['info_dict']['id'], percent)
        except:
            pass

    ydl_opts = {
        'quiet': False,
        'ignoreerrors': True,
        'noplaylist': True,
        # 'restrictfilenames': True,

        'paths': {'home': os.environ["HOME"]},  # save in local app storage

        'writesubtitles': preset.subtitles,
        'subtitleslangs': preset.subtitlesLanguage,
        'subtitlesformat': preset.subtitleFormat,

        'writethumbnail': preset.thumbnails,

        'format': preset.format,
        'outtmpl': {'default': preset.outputTemplate},

        'postprocessors': [],  # post processors can be added later
        'postprocessor_hooks': [postHook],
        'progress_hooks': [progressHook],

        'ffmpeg_location': ffmpegPath
    }

    if preset.extractAudio:
        ydl_opts['postprocessors'].append(
            {
                'key': 'FFmpegExtractAudio',
                'preferredquality': 0
            }
        )

    if preset.thumbnails:
        ydl_opts['postprocessors'].append(
            {
                'key': 'EmbedThumbnail'
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

    #if preset.archive != '':
     #   ydl_opts['download_archive'] = preset.archive

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        try:
            ydl.extract_info(url)
        except:
            itemErrorCallback.invoke(url)


def tagVideo(videoInfo: dict):
    tagger = Tagger.Tagger()

    tagger.setTags(videoInfo)


# downloadVideo("https://www.youtube.com/watch?v=VLp8x54JmfA", None)
