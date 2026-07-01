package top.hcode.hoj.dao.pk.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.pk.PkMatchEntityService;
import top.hcode.hoj.mapper.PkMatchMapper;
import top.hcode.hoj.pojo.entity.pk.PkMatch;
import top.hcode.hoj.pojo.vo.PkMatchVO;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description: PK对战实体服务实现
 */
@Service
public class PkMatchEntityServiceImpl extends ServiceImpl<PkMatchMapper, PkMatch> implements PkMatchEntityService {

    @Resource
    private PkMatchMapper pkMatchMapper;

    @Override
    public PkMatchVO getPkMatchById(Long id) {
        return pkMatchMapper.selectPkMatchById(id);
    }

    @Override
    public List<PkMatchVO> getActivePkMatchByUserId(String uid) {
        return pkMatchMapper.selectActivePkMatchByUserId(uid);
    }

    @Override
    public List<PkMatchVO> getPendingPkMatchByUserId(String uid) {
        return pkMatchMapper.selectPendingPkMatchByUserId(uid);
    }

    @Override
    public IPage<PkMatchVO> getPkHistory(Page<PkMatchVO> page, String uid) {
        return pkMatchMapper.selectPkHistory(page, uid);
    }
}
