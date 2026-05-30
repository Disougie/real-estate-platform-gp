package com.disougie.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.disougie.property.entity.Property;
import com.disougie.property.entity.PropertyType;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchRepository {
	
	private final MongoTemplate mongoTemplate;
	
	@Override
	public Page<Property> findByText(String text, int page, int size) {
		
		Pageable pageable = PageRequest.of(page, size);
		
		// MongoDB Atlas search query
		Document searchDocsStage = new Document("$search", 
			    new Document("index", "property_search_index")
	            .append("compound", new Document("must", Arrays.asList(new Document("text", 
	                    new Document("query", text)
	                            .append("path", Arrays.asList("title", "description", "location.city", "location.area")))))
	                .append("filter", Arrays.asList(new Document("text", 
	                    new Document("query", "AVAILABLE")
	                            .append("path", "status")))))
		);

		// Build aggregation pipeline search -> skip -> limit
	    Aggregation dataAggregation = Aggregation.newAggregation(
	        context -> searchDocsStage,
	        Aggregation.skip(pageable.getOffset()),
	        Aggregation.limit(pageable.getPageSize())
	    );

	    List<Property> content = mongoTemplate
	    		.aggregate(dataAggregation, "property", Property.class)
	    		.getMappedResults();


	    // for getting the total number of property satisfying the query 
	    Document searchMetaStage = new Document("$searchMeta", 
	        new Document("index", "property_search_index")
	        .append("count", new Document("type", "total")) // هذا يطلب العدد الكلي
	        .append("compound", new Document("must", Arrays.asList(new Document("text", 
                    new Document("query", text)
                            .append("path", Arrays.asList("title", "description", "location.city", "location.area")))))
                .append("filter", Arrays.asList(new Document("text", 
                    new Document("query", "AVAILABLE")
                            .append("path", "status")))))
	    );

	    Aggregation metaAggregation = Aggregation
	    		.newAggregation(context -> searchMetaStage);
	    
	    Document metaResult = mongoTemplate
	    		.aggregate(metaAggregation, "property", Document.class)
	    		.getUniqueMappedResult();

	    long total = 0;
	    if (metaResult != null && metaResult.containsKey("count")) {
	        Document countDoc = (Document) metaResult.get("count");
	        total = countDoc.getLong("total"); 
	    }

	    return new PageImpl<>(content, pageable, total);
		
	}

	@Override
	public Page<Property> findByTextForAdmin(String text, int page, int size) {
		
		Pageable pageable = PageRequest.of(page, size);
		
		Document searchDocsStage = new Document("$search", 
			    new Document("index", "property_search_index")
	            .append("compound", new Document("must", Arrays.asList(new Document("text", 
	                    new Document("query", text)
	                            .append("path", Arrays.asList("title", "description", "location.city", "location.area")))))
//	                .append("filter", Arrays.asList(new Document("text", 
//	                    new Document("query", "AVAILABLE")
//	                            .append("path", "status"))))
	            )
		);

	    // بناء Pipeline البيانات: Search -> Skip -> Limit
	    Aggregation dataAggregation = Aggregation.newAggregation(
	        context -> searchDocsStage,
	        Aggregation.skip(pageable.getOffset()),
	        Aggregation.limit(pageable.getPageSize())
	    );

	    List<Property> content = mongoTemplate
	    		.aggregate(dataAggregation, "property", Property.class)
	    		.getMappedResults();


	    // --- الاستعلام الثاني: جلب العدد الكلي (The Total Count) ---
	    Document searchMetaStage = new Document("$searchMeta", 
	        new Document("index", "property_search_index")
	        .append("count", new Document("type", "total")) // هذا يطلب العدد الكلي
	        .append("compound", new Document("must", Arrays.asList(new Document("text", 
                    new Document("query", text)
                            .append("path", Arrays.asList("title", "description", "location.city", "location.area")))))
                .append("filter", Arrays.asList(new Document("text", 
                    new Document("query", "AVAILABLE")
                            .append("path", "status")))))
	    );

	    Aggregation metaAggregation = Aggregation
	    		.newAggregation(context -> searchMetaStage);
	    
	    Document metaResult = mongoTemplate
	    		.aggregate(metaAggregation, "property", Document.class)
	    		.getUniqueMappedResult();

	    long total = 0;
	    if (metaResult != null && metaResult.containsKey("count")) {
	        Document countDoc = (Document) metaResult.get("count");
	        total = countDoc.getLong("total"); // أو getLong حسب حجم بياناتك
	    }

	    return new PageImpl<>(content, pageable, total);

	}
	

	@Override
	public Page<Property> findByFilters(PropertyType type,
			String city, String area, Integer minRooms, Integer maxRooms,Integer minBaths,
			Integer maxBaths, Double minPrice, Integer maxPrice, Integer minSize,
			Integer maxSize, int page, int size
		) {
		
		ArrayList<Bson> pipeline = new ArrayList<>();
		
		pipeline.add(Aggregates.match(Filters.eq("status", "AVAILABLE")));
		
		if(type != null) {
			pipeline.add(Aggregates.match(Filters.eq("type", type)));			
		}
		
		if(city != null && !city.isBlank()) {
			pipeline.add(Aggregates.match(Filters.eq("location.city",city)));
		}
		
		if(area != null && !area.isBlank()) {
			pipeline.add(Aggregates.match(Filters.eq("location.area",area)));
		}
		
		if(minRooms != null) {
			pipeline.add(Aggregates.match(Filters.gte("features.rooms", minRooms)));
		}
		
		if(maxRooms != null) {
			pipeline.add(Aggregates.match(Filters.lte("features.rooms",maxRooms)));
		}
		
		if(minBaths != null) {
			pipeline.add(Aggregates.match(Filters.gte("features.baths",minBaths)));
		}
		
		if(maxBaths != null) {
			pipeline.add(Aggregates.match(Filters.lte("features.baths",maxBaths)));
		}
		
		if(minPrice != null) {
			pipeline.add(Aggregates.match(Filters.gte("price",minPrice)));
		}
		
		if(maxPrice != null) {
			pipeline.add(Aggregates.match(Filters.lte("price",maxPrice)));
		}
		
		if(minSize != null) {
			pipeline.add(Aggregates.match(Filters.gte("features.size",minSize)));
		}
		
		if(maxSize != null) {
			pipeline.add(Aggregates.match(Filters.lte("features.size",maxSize)));
		}
		
		
		ArrayList<Bson> countPipeline = new ArrayList<>(pipeline);
		countPipeline.add(Aggregates.count("count"));
		
		Document countDocument = mongoTemplate
				.getCollection("property")
				.aggregate(countPipeline)
				.first();
		
		Integer totalElements = countDocument != null ? countDocument.getInteger("count") : 0;
		
		int totalPages = (int) Math.ceil(totalElements / (size * 1.0));
		
		if(page >= totalPages)
			return new PageImpl <Property>(List.of(), PageRequest.of(page, size), totalElements);
		
		pipeline.add(Aggregates.sort(Sorts.ascending("price")));
				
		List<Property> result = mongoTemplate
				.getCollection("property")
				.aggregate(pipeline)
				.map(doc -> mongoTemplate.getConverter().read(Property.class, doc))
				.into(new ArrayList<>());

		int end = page * size + size > result.size() ? result.size() : page * size + size; 
		
		return new PageImpl<Property>(
				result.subList(page * size, end), 
				PageRequest.of(page, size),
				totalElements
		);
		
	}


	@Override
	public List<Property> findNearByCoordinates(double lng, double lat, double maxDistance) {
		
		Query query = new Query();
		double earth_radius = 6378137.0;
		
		if(maxDistance > 10)
			maxDistance = 10 ;
		
		query.addCriteria(
				Criteria
					.where("mapsLocation")
					.nearSphere(new Point(lng, lat))
					.maxDistance((maxDistance * 1000) / earth_radius )
					.minDistance(0)
		);
		
		query.addCriteria(
				Criteria
					.where("status")
					.is("AVAILABLE")
		);
		
		return mongoTemplate.find(query, Property.class);
	}
	
}
