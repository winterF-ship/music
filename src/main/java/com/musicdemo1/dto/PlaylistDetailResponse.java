package com.musicdemo1.dto;

import com.musicdemo1.entity.SongList;
import java.util.List;

public record PlaylistDetailResponse(SongList playlist, List<SongView> songs) {
}
