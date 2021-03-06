package com.wugui.datax.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.ApiController;
import com.baomidou.mybatisplus.extension.api.R;
import com.wugui.datax.admin.core.util.LocalCacheUtil;
import com.wugui.datax.admin.entity.JobDatasourceEntity;
import com.wugui.datax.admin.service.JobDatasourceService;
import com.wugui.datax.admin.util.AESUtil;
import com.wugui.datax.admin.util.PageUtils;
import com.wugui.datax.admin.entity.JobDatasource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * jdbc数据源配置控制器层
 *
 * @author zhouhongfa@gz-yibo.com
 * @version v1.0
 * @since 2019-07-30
 */
@RestController
@RequestMapping("/api/jobJdbcDatasource")
@Api(tags = "jdbc数据源配置接口")
public class JobDatasourceController extends ApiController {
    /**
     * 服务对象
     */
    @Autowired
    private JobDatasourceService jobJdbcDatasourceService;

    /**
     * 分页查询所有数据
     *
     * @return 所有数据
     */
    @GetMapping
    @ApiOperation("分页查询所有数据")
    @ApiImplicitParams(
            {@ApiImplicitParam(paramType = "query", dataType = "String", name = "current", value = "当前页", defaultValue = "1", required = true),
                    @ApiImplicitParam(paramType = "query", dataType = "String", name = "size", value = "一页大小", defaultValue = "10", required = true),
                    @ApiImplicitParam(paramType = "query", dataType = "Boolean", name = "ifCount", value = "是否查询总数", defaultValue = "true"),
                    @ApiImplicitParam(paramType = "query", dataType = "String", name = "ascs", value = "升序字段，多个用逗号分隔"),
                    @ApiImplicitParam(paramType = "query", dataType = "String", name = "descs", value = "降序字段，多个用逗号分隔")
            })
    //TODO  不确定该接口哪里用到，但是想把current修改pageNo，size修改为pageSize
    public R<IPage<JobDatasource>> selectAll() {
        BaseForm form = new BaseForm();
        QueryWrapper<JobDatasource> query = (QueryWrapper<JobDatasource>) form.pageQueryWrapperCustom(form.getParameters(), new QueryWrapper<JobDatasource>());
        return success(jobJdbcDatasourceService.page(form.getPlusPagingQueryEntity(), query));
    }
    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @ApiOperation("通过主键查询单条数据")
    @GetMapping("{id}")
    public R<JobDatasource> selectOne(@PathVariable Serializable id) {
        JobDatasource jobDatasource=this.jobJdbcDatasourceService.getById(id);
        //如果用户名和密码不为空 需要进行解密传重新复制回给页面
        if (StringUtils.isNotBlank(jobDatasource.getJdbcUsername())){
            String userName = AESUtil.decrypt(jobDatasource.getJdbcUsername());
            jobDatasource.setJdbcUsername(userName);
        }
        if (StringUtils.isNotBlank(jobDatasource.getJdbcPassword())){
            String password = AESUtil.decrypt(jobDatasource.getJdbcPassword());
            jobDatasource.setJdbcPassword(password);
        }
        return success(jobDatasource);
    }

    /**
     * 新增数据
     *
     * @param entity 实体对象
     * @return 新增结果
     */
    @ApiOperation("新增数据")
    @PostMapping
    public R<Boolean> insert(@RequestBody JobDatasource entity) {
        return success(this.jobJdbcDatasourceService.save(entity));
    }

    /**
     * 修改数据
     *
     * @param entity 实体对象
     * @return 修改结果
     */
    @PutMapping
    @ApiOperation("修改数据")
    public R<Boolean> update(@RequestBody JobDatasource entity) {
        LocalCacheUtil.remove(entity.getDatasource());
        JobDatasource d = jobJdbcDatasourceService.getById(entity.getId());
        //如果是hive不需要用户名和密码
        if(!"hive".equals(d.getDatasource())){
            if (entity.getJdbcUsername().equals(d.getJdbcUsername())) {
                entity.setJdbcUsername(null);
            }
            if (entity.getJdbcPassword().equals(d.getJdbcPassword())) {
                entity.setJdbcPassword(null);
            }
        }
            return success(this.jobJdbcDatasourceService.updateById(entity));

    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    @ApiOperation("删除数据")
    public R<Boolean> delete(@RequestParam("idList") List<Long> idList) {
        return success(this.jobJdbcDatasourceService.removeByIds(idList));
    }

    /**
     * 测试数据源
     * @param jobJdbcDatasource
     * @return
     */
    @PostMapping("/test")
    @ApiOperation("测试数据")
    public R<Boolean> dataSourceTest (@RequestBody JobDatasourceEntity jobJdbcDatasource) throws IOException, SQLException, ClassNotFoundException {
        return success(jobJdbcDatasourceService.dataSourceTest(jobJdbcDatasource));
    }
}