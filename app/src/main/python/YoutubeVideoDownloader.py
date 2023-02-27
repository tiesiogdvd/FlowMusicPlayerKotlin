import yt_dlp
import os
import copy
import json

import Presets

def dictFilter(dictionary: dict, keysToKeep=None):
    if keysToKeep is None:
        keysTokeep = ['title', 'playlist_index', 'album',
                    'artist', 'release_date','playlist',
                    'filepath', 'id']

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
        try:
            pass
            # progress_callback.emit(int(info['info_dict']['playlist_index']))
        except:
            pass

    elif info['status'] == 'downloading':
        #print(
        #    info['downloaded_bytes'],
        #    info['total_bytes'],
        #    info['speed'],
        #    info['eta'],
        #    sep=' | ', end='\n'
        #)
        pass

"""         progress_callback.invoke(
            int(info['total_bytes']),
            int(info['downloaded_bytes']),
            int(info['speed']),
            int(info['eta'])
        ) """


def getInfo(url: str, callback):
    print(url)
    print(callback)

    def postHook(info: dict):
        #print(info.keys())
        print(info['info_dict'].keys())

        if info['status'] == 'finished' and info['postprocessor'] == 'MoveFiles':
            videosInfo.append(dictFilter(info['info_dict']))

            #print('playlist: ')
            #print(info['info_dict']['playlist'])

            playlist = ''

            if info['info_dict']['playlist'] is not None:
                playlist = info['info_dict']['playlist']

            callback(len(videosInfo), info['info_dict']['title'], playlist)

    ydl_opts = {
        'quiet': False,
        'ignoreerrors': True,
        'skip_download': True,
        'postprocessor_hooks': [postHook],
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        ydl.extract_info(url)

    print(videosInfo)

def downloadVideo(url: str, ffmpegExecutable: str, callback):
    print(os.environ["HOME"])
    print(ffmpegExecutable)

    callback.invoke(1, 1, 1, 1)

    global progress_callback
    progress_callback = callback

    preset = Presets.audioPreset

    def postHook(info: dict):
        if info['status'] == 'finished' and info['postprocessor'] == 'MoveFiles':
            if saveChapters == True and type(info['info_dict']['chapters']) != 'dict' and info['info_dict']['chapters'] != None: 
                writeChapters(info['info_dict']['chapters'],
                            info['info_dict']['filepath'])

            videosInfo.append(dictFilter(info['info_dict']))

    ydl_opts = {
        'quiet': False,
        'ignoreerrors': True,
        
        'restrictfilenames': True,

        'paths': { 'home': os.environ["HOME"] }, # save in local app storage

        'writesubtitles': preset.subtitles,
        'subtitleslangs': preset.subtitlesLanguage,
        'subtitlesformat': preset.subtitleFormat,

        'writethumbnail': preset.thumbnails,

        'format': preset.format,
        'outtmpl': {'default': preset.outputTemplate},

        'postprocessors': [], # post processors can be added later
        'postprocessor_hooks': [postHook],
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