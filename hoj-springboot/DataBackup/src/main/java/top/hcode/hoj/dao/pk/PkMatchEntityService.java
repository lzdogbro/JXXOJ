package top.hcode.hoj.dao.pk;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.pk.PkMatch;
import top.hcode.hoj.pojo.vo.PkMatchVO;

import java.util.List;

/**
 * @Description: PK对战实体服务接口
 */
public interface PkMatchEntityService extends IService<PkMatch> {

    PkMatchVO getPkMatchById(Long id);

    List<PkMatchVO> getActivePkMatchByUserId(String uid);

    List<PkMatchVO> getPendingPkMatchByUserId(String uid);

    List<PkMatchVO> getMyAllPendingInvites(String uid);

    IPage<PkMatchVO> getPkHistory(Page<PkMatchVO> page, String uid);
}
