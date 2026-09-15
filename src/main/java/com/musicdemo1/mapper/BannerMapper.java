package com.musicdemo1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.musicdemo1.entity.Banner;
import java.util.List;
import org.apache.ibatis.annotations.Select;

public interface BannerMapper extends BaseMapper<Banner> {
    @Select("SELECT id, title, image_url, target_url, sort_no, status, created_at, updated_at FROM banner WHERE status = 1 ORDER BY sort_no ASC, id DESC")
    List<Banner> selectActive();
}
