package com.verint.textanalytics.dal.darwin;

import java.util.ArrayList;
import java.util.List;

import com.verint.textanalytics.common.utils.StringUtils;
import com.verint.textanalytics.model.interactions.SpeakerType;

import lombok.Getter;
import lombok.Setter;

/**
 * Solr collection status.
 * @author EZlotnik
 *
 */
class CollectionStatus {
	@Getter
	@Setter
	private List<Shard> shards;

	public CollectionStatus() {
		this.shards = new ArrayList<>();
	}

	public void addShard(Shard shard) {
		this.shards.add(shard);
	}
}

/**
 * Solr collection shard.
 * @author EZlotnik
 *
 */
class Shard {
	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private ShardState state;

	@Getter
	@Setter
	private List<Replica> replicas;

	public Shard() {
		this.replicas = new ArrayList<>();
	}

	public void addReplica(Replica replica) {
		this.replicas.add(replica);
	}
}

/**
 * Solr shard's replica.
 * @author EZlotnik
 *
 */
class Replica {
	@Getter
	@Setter
	private String core;

	@Getter
	@Setter
	private String baseUrl;

	@Getter
	@Setter
	private String nodeName;

	@Getter
	@Setter
	private ReplicaState state;
}

/**
 * Shard state.
 * @author EZlotnik
 *
 */
enum ShardState {
	Active, Inactive;

	public static ShardState toShardState(String value) {
		ShardState state = Inactive;

		if (!StringUtils.isNullOrBlank(value)) {
			switch (value.toLowerCase()) {
				case "active":
					state = Active;
					break;
				case "inactive":
				case "":
				default:
					state = Inactive;
					break;
			}
		}

		return state;
	}
}

/**
 * Replica state.
 * @author EZlotnik
 *
 */
enum ReplicaState {
	Active, Inactive;

	public static ReplicaState toReplicaState(String value) {
		ReplicaState state = Inactive;

		if (!StringUtils.isNullOrBlank(value)) {
			switch (value.toLowerCase()) {
				case "active":
					state = Active;
					break;
				case "inactive":
				case "":
				default:
					state = Inactive;
					break;
			}
		}

		return state;
	}
}
