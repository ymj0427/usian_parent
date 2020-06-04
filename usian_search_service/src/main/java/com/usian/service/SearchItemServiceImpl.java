package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.usian.mapper.SearchItemMapper;
import com.usian.pojo.SearchItem;
import com.usian.utils.JsonUtils;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class SearchItemServiceImpl implements SearchItemService {

    @Autowired
    private SearchItemMapper searchItemMapper;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Value("${ES_INDEX_NAME}")
    private String ES_INDEX_NAME;

    @Value("${ES_TYPE_NAME}")
    private String ES_TYPE_NAME;

    @Override
    public boolean importAll() {
        try {
            if (!isExistsIndex()) {
                createIndex();
            }
            int page = 1;
            while (true){
                // 分页每次导图一千条
                PageHelper.startPage(page,1000);
                //1、查询mysql中的商品信息
                List<SearchItem> searchItemList = searchItemMapper.getItemList();
                if (searchItemList == null || searchItemList.size()==0){
                    break;
                }
                BulkRequest bulkRequest = new BulkRequest();
                for (int i =0;i < searchItemList.size();i++){
                    SearchItem searchItem = searchItemList.get(i);
                    //2、把商品信息添加到es中
                    bulkRequest.add(new IndexRequest(ES_INDEX_NAME,ES_TYPE_NAME)
                            .source(JsonUtils.objectToJson(searchItem),XContentType.JSON));
                }
                restHighLevelClient.bulk(bulkRequest,RequestOptions.DEFAULT);
                page++;
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 索引路是否存在
     * @return
     */
    public boolean isExistsIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(ES_INDEX_NAME);
        return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }

    public boolean createIndex() throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(ES_INDEX_NAME);
        createIndexRequest.settings(Settings.builder().put("number_of_shards",2)
                .put("number_of_replicas",1));
        createIndexRequest.mapping(ES_TYPE_NAME,"{\n" +
                "  \"_source\": {\n" +
                "    \"excludes\": [\n" +
                "      \"item_desc\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"properties\": {\n" +
                "    \"id\":{\n" +
                "      \"type\": \"keyword\"\n" +
                "    },\n" +
                "    \"item_title\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    },\n" +
                "    \"item_sell_point\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    },\n" +
                "    \"item_price\": {\n" +
                "      \"type\": \"float\"\n" +
                "    },\n" +
                "    \"item_image\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"index\": false\n" +
                "    },\n" +
                "    \"item_category_name\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    },\n" +
                "    \"item_desc\": {\n" +
                "      \"type\": \"text\",\n" +
                "      \"analyzer\": \"ik_max_word\",\n" +
                "      \"search_analyzer\": \"ik_smart\"\n" +
                "    }\n" +
                "  }\n" +
                "}", XContentType.JSON);
        //创建索引操作客户端
        IndicesClient indices = restHighLevelClient.indices();
        //创建响应对象
        CreateIndexResponse response = indices.create(createIndexRequest, RequestOptions.DEFAULT);
        return response.isAcknowledged();
    }
}
