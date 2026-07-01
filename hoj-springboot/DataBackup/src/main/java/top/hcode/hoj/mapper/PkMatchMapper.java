package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.pk.PkMatch;
import top.hcode.hoj.pojo.vo.PkMatchVO;

import java.util.List;

@Mapper
@Repository
public interface PkMatchMapper extends BaseMapper<PkMatch> {

    PkMatchVO selectPkMatchById(@Param("id") Long id);

    List<PkMatchVO> selectActivePkMatchByUserId(@Param("uid") String uid);

    List<PkMatchVO> selectPendingPkMatchByUserId(@Param("uid") String uid);

    List<PkMatchVO> selectMyAllPendingInvites(@Param("uid") String uid);

    IPage<PkMatchVO> selectPkHistory(Page<PkMatchVO> page, @Param("uid") String uid);
}
