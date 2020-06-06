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
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Override
    public List<SearchItem> selectByq(String q, Long page, Integer pageSize) throws IOException {
        try{
            SearchRequest searchRequest = new SearchRequest(ES_INDEX_NAME);
            searchRequest.types(ES_TYPE_NAME);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            //1、查询名字、描述、卖点、类别包括“q”的商品
            searchSourceBuilder.query(QueryBuilders.multiMatchQuery(q,new String[]{
                    "item_title","item_desc","item_sell_point","item_category_name"}));
            //2、分页
            /**
             * 1  0  20--->(p-1)*pageSize
             * 2  20 20--->(2-1)*20
             * 3  40 20--->(3-1)*20
             */
            Long  from = (page - 1) * pageSize;
            searchSourceBuilder.from(from.intValue());
            searchSourceBuilder.size(pageSize);
            //3、高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<font color='red'>");
            highlightBuilder.postTags("</font>");
            highlightBuilder.field("item_title");
            searchSourceBuilder.highlighter(highlightBuilder);

            searchRequest.source(searchSourceBuilder);
            SearchResponse response = restHighLevelClient.search(
                    searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            //4、返回查询结果
            List<SearchItem> searchItemList = new ArrayList<>();
            for (int i=0; i<hits.length; i++){
                SearchHit hit = hits[i];
                SearchItem searchItem = JsonUtils.jsonToPojo(hit.getSourceAsString(),
                        SearchItem.class);
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if(highlightFields!=null && highlightFields.size()>0) {
                    searchItem.setItem_title(highlightFields.get("item_title").
                            getFragments()[0].toString());
                }
                searchItemList.add(searchItem);
            }
            return searchItemList;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加商品信息到索引库
     * @param msg
     * @return
     * @throws IOException
     */
    @Override
    public int addDocement(String msg) throws IOException {
        //1、根据商品id查询商品信息
        SearchItem searchItem = searchItemMapper.getItemById(Long.valueOf(msg));

        //2、添加商品到索引库
        IndexRequest indexRequest = new IndexRequest(ES_INDEX_NAME, ES_TYPE_NAME);
        indexRequest.source(JsonUtils.objectToJson(searchItem),XContentType.JSON);
        IndexResponse response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        return response.getShardInfo().getFailed();
    }

    /**
     * 索引库是否存在
     * @return
     */
    public boolean isExistsIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest();
        getIndexRequest.indices(ES_INDEX_NAME);
        return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 创建索引库
     * @return
     * @throws IOException
     */
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
