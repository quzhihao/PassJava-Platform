package com.jackson0714.passjava.question.service.impl;

import com.jackson0714.common.to.es.QuestionEsModel;
import com.jackson0714.common.utils.R;
import com.jackson0714.passjava.question.entity.TypeEntity;
import com.jackson0714.passjava.question.feign.SearchFeignService;
import com.jackson0714.passjava.question.service.ITypeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jackson0714.common.utils.PageUtils;
import com.jackson0714.common.utils.Query;

import com.jackson0714.passjava.question.dao.QuestionDao;
import com.jackson0714.passjava.question.entity.QuestionEntity;
import com.jackson0714.passjava.question.service.IQuestionService;


@Service("questionService")
public class QuestionServiceImpl extends ServiceImpl<QuestionDao, QuestionEntity> implements IQuestionService {

    @Autowired
    ITypeService typeService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        //1.get key
        String key = (String) params.get("key");
        QueryWrapper<QuestionEntity> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("id", key).or().like("title", key).or().like("answer", key);
        }

        String type = (String) params.get("type");
        if (!StringUtils.isEmpty(type)) {
            queryWrapper.eq("type", type);
        }
        IPage<QuestionEntity> page = this.page(
                new Query<QuestionEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public boolean saveQuestion(QuestionEntity question) {
        boolean saveResult = save(question);
        //saveEs(question);

        return true;
    }

    @Override
    public boolean updateQuestion(QuestionEntity question) {
        updateById(question);
        //saveEs(question);

        return true;
    }

    private boolean saveEs(QuestionEntity question) {
        // 1.?????? ES model
        QuestionEsModel esModel = new QuestionEsModel();
        // 2.????????????
        // 2.1 ????????????
        BeanUtils.copyProperties(question, esModel);
        // 2.2 ?????????????????????????????????
        TypeEntity typeEntity = typeService.getById(question.getType());
        String typeName = typeEntity.getType();
        // 2.3 ??? ES model ???????????????????????????
        esModel.setTypeName(typeName);
        System.out.println("-----------------esModel:" + esModel);

        // 3. ?????? passjava-search ??????????????????????????? ES ????????????
        R r = searchFeignService.saveQuestion(esModel);

        System.out.println("r:" + r);

        return true;
    }

}