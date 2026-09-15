package com.musicdemo1.dto;

import com.musicdemo1.entity.Singer;
import java.util.List;

public record SingerDetailResponse(Singer singer, List<SongView> songs) {
}
