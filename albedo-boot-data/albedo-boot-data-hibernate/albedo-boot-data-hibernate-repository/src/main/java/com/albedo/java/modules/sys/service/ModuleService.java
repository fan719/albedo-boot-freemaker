package com.albedo.java.modules.sys.service;

import com.albedo.java.common.data.persistence.DynamicSpecifications;
import com.albedo.java.common.domain.base.BaseEntity;
import com.albedo.java.common.service.TreeService;
import com.albedo.java.common.service.TreeVoService;
import com.albedo.java.modules.sys.domain.Module;
import com.albedo.java.modules.sys.repository.ModuleRepository;
import com.albedo.java.util.PublicUtil;
import com.albedo.java.util.StringUtil;
import com.albedo.java.util.domain.QueryCondition;
import com.albedo.java.util.domain.RequestMethod;
import com.albedo.java.vo.sys.ModuleVo;
import com.albedo.java.vo.sys.query.ModuleMenuTreeResult;
import com.albedo.java.vo.sys.query.ModuleTreeQuery;
import com.albedo.java.vo.sys.query.TreeResult;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service class for managing modules.
 */
@Service
@Transactional
public class ModuleService extends TreeVoService<ModuleRepository, Module, String, ModuleVo> {

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<ModuleMenuTreeResult> findMenuData(ModuleTreeQuery moduleTreeQuery, List<Module> moduleList) {
        String type = moduleTreeQuery != null ? moduleTreeQuery.getType() : null,
                all = moduleTreeQuery != null ? moduleTreeQuery.getAll() : null;

        List<ModuleMenuTreeResult> mapList = Lists.newArrayList();
        for (Module e : moduleList) {
            ModuleMenuTreeResult moduleMenuTreeResult = null;
            if ((all != null || (all == null && BaseEntity.FLAG_NORMAL.equals(e.getStatus())))) {

                if ("menu".equals(type) && !Module.TYPE_MENU.equals(e.getType())) {
                    continue;
                }
                if (moduleTreeQuery != null && moduleTreeQuery.getRoot() && PublicUtil.isEmpty(e.getParentId())) {
                    continue;
                }

                moduleMenuTreeResult = new ModuleMenuTreeResult();
                moduleMenuTreeResult.setId(e.getId());
                moduleMenuTreeResult.setBpid(e.getParentId() != null ? e.getParentId() : "0");
                moduleMenuTreeResult.setMpid(moduleMenuTreeResult.getBpid());
                moduleMenuTreeResult.setName(e.getName());
                moduleMenuTreeResult.setRoute(e.getHref());
                moduleMenuTreeResult.setIcon(e.getIconCls());
                mapList.add(moduleMenuTreeResult);
            }
        }
        return mapList;
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<TreeResult> findTreeData(ModuleTreeQuery moduleTreeQuery, List<Module> moduleList) {
        String type = moduleTreeQuery != null ? moduleTreeQuery.getType() : null,
                all = moduleTreeQuery != null ? moduleTreeQuery.getAll() : null;

        List<TreeResult> mapList = Lists.newArrayList();
        for (Module e : moduleList) {
            TreeResult treeResult = null;
            if ((all != null || (all == null && BaseEntity.FLAG_NORMAL.equals(e.getStatus())))) {

                if ("menu".equals(type) && !Module.TYPE_MENU.equals(e.getType())) {
                    continue;
                }
                if (moduleTreeQuery != null && moduleTreeQuery.getRoot() && PublicUtil.isEmpty(e.getParentId())) {
                    continue;
                }
                treeResult = new TreeResult();
                treeResult.setId(e.getId());
                treeResult.setPid(e.getParentId() != null ? e.getParentId() : "0");
                treeResult.setLabel(e.getName());
                treeResult.setKey(e.getName());
                treeResult.setValue(e.getId());
                mapList.add(treeResult);
            }
        }
        return mapList;
    }


//    @Transactional(readOnly = true, rollbackFor = Exception.class)
//    public List<Module> findAllByParentId(String parentId) {
//        return repository.findAllByParentIdAndStatusNot(parentId, Module.FLAG_DELETE);
//    }

    public void generatorModuleData(String moduleName, String parentModuleId, String url) {
        Module currentModule = repository.findOne(DynamicSpecifications.bySearchQueryCondition(QueryCondition.eq(Module.F_NAME, moduleName)));
        if (currentModule != null) {
            baseRepository.execute("delete Module where id=:p1 or parentId=:p1", currentModule.getId());
        }
        Module parentModule = repository.findOne(parentModuleId);
        if (parentModule == null) {
            new Exception(PublicUtil.toAppendStr("根据模块id[", parentModuleId, "无法查询到模块信息]"));
        }
        String permission = url.replace("/", "_").substring(1);
        Module module = new Module();
        module.setPermission(permission.substring(0, permission.length() - 1));
        module.setName(moduleName);
        module.setParentId(parentModule.getId());
        module.setType(Module.TYPE_MENU);
        module.setRequestMethod(RequestMethod.GET);
        module.setIconCls("fa-file");
        module.setUrl(url);
        save(module);

        Module moduleView = new Module();
        moduleView.setParent(module);
        moduleView.setName("查看");
        moduleView.setIconCls("fa-info-circle");
        moduleView.setPermission(permission + "view");
        moduleView.setParentId(module.getId());
        moduleView.setType(Module.TYPE_OPERATE);
        moduleView.setRequestMethod(RequestMethod.GET);
        moduleView.setSort(20);
        moduleView.setUrl(url + "page");
        save(moduleView);
        Module moduleEdit = new Module();
        moduleEdit.setParent(module);
        moduleEdit.setName("编辑");
        moduleEdit.setIconCls("fa-pencil");
        moduleEdit.setPermission(permission + "edit");
        moduleEdit.setParentId(module.getId());
        moduleEdit.setType(Module.TYPE_OPERATE);
        moduleEdit.setSort(40);
        moduleEdit.setUrl(url + "edit");
        moduleEdit.setRequestMethod(PublicUtil.toAppendStr(RequestMethod.GET, StringUtil.SPLIT_DEFAULT, RequestMethod.POST));
        save(moduleEdit);
        Module moduleLock = new Module();
        moduleLock.setParent(module);
        moduleLock.setName("锁定");
        moduleLock.setIconCls("fa-lock");
        moduleLock.setPermission(permission + "lock");
        moduleLock.setParentId(module.getId());
        moduleLock.setType(Module.TYPE_OPERATE);
        moduleLock.setSort(60);
        moduleLock.setUrl(url + "lock");
        moduleLock.setRequestMethod(RequestMethod.POST);
        save(moduleLock);
        Module moduleDelete = new Module();
        moduleDelete.setParent(module);
        moduleDelete.setName("删除");
        moduleDelete.setIconCls("fa-trash-o");
        moduleDelete.setPermission(permission + "delete");
        moduleDelete.setParentId(module.getId());
        moduleDelete.setType(Module.TYPE_OPERATE);
        moduleDelete.setSort(80);
        moduleDelete.setUrl(url + "delete");
        moduleDelete.setRequestMethod(RequestMethod.DELETE);
        save(moduleDelete);

    }

    /**
     * 根据用户获取权限
     *
     * @param userId
     * @return
     */
    public List<Module> findAllAuthByUser(String userId) {
        return baseRepository.findListByHQL("select distinct m from Module m, Role r, User u where m in elements (r.modules) and r in elements (u.roles) and m.status=0 and r.status=0 and u.status=0 and u.id=:p1 order by m.sort", userId);
    }

    public List<Module> findAllByStatusOrderBySort(Integer flagNormal) {
        return repository.findAllByStatusOrderBySort(flagNormal);
    }
}
