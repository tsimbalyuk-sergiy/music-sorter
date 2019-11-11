MUSIC-SORTER
========================
Small tool to sort new music albums.

Sorting based on format(`mp3`, `flac`) and genre.


At first start app will create sample configuration file in directory:

`$HOME/.config/music-sorter/music-sorter.properties`

All folders with be moved to corresponding directories based on file extensions and metadata genre

e.g.:

```
■ root
├──■ flac
│  └──■  Some Genre
│     └──■  First Folder
│        ├── ■ file.jpg
│        ├── ■ file.m3u
│        └── ■ file.flac
└──■ mp3
   ├──■  Another Genre
   │  └──■  Second Folder
   │     ├── ■ file.jpg
   │     └── ■ file.mp3
   └──■  Another One Genre
      └──■  Third Folder
         ├── ■ file.jpg
         └── ■ file.mp3
```
