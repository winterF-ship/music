package com.musicdemo1.service;

import com.musicdemo1.dto.DashboardSummary;
import com.musicdemo1.dto.CountItem;
import java.util.List;
import com.musicdemo1.entity.MusicUser;
import com.musicdemo1.entity.Singer;
import com.musicdemo1.entity.Song;
import com.musicdemo1.entity.SongList;
import com.musicdemo1.mapper.MusicUserMapper;
import com.musicdemo1.mapper.SingerMapper;
import com.musicdemo1.mapper.SongListMapper;
import com.musicdemo1.mapper.SongMapper;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final MusicUserMapper userMapper;
    private final SingerMapper singerMapper;
    private final SongMapper songMapper;
    private final SongListMapper songListMapper;

    public DashboardService(MusicUserMapper userMapper, SingerMapper singerMapper, SongMapper songMapper,
            SongListMapper songListMapper) {
        this.userMapper = userMapper;
        this.singerMapper = singerMapper;
        this.songMapper = songMapper;
        this.songListMapper = songListMapper;
    }

    public DashboardSummary summary() {
        return new DashboardSummary(userMapper.selectCount(null), singerMapper.selectCount(null),
                songMapper.selectCount(null), songListMapper.selectCount(null));
    }

    public List<CountItem> songsBySinger() {
        return songMapper.countSongsBySinger();
    }
}
