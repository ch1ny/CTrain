<template>
	<div>
		<el-container>
			<el-main>
				<el-card>
					<div>
						<el-descriptions class="margin-top" :column="3" border>
							<template slot="title">
								<span><b>{{train}}次列车座位状态</b></span>
							</template>
							<el-descriptions-item>
								<template slot="label">
									<i class="el-icon-s-order"></i>
									座位状态
								</template>
								<template>
									<el-tag v-if="hasSeats" effect="dark"><i class="el-icon-s-claim"></i>有座位表</el-tag>
									<el-tag v-if="!hasSeats" effect="dark" type="danger"><i class="el-icon-s-release"></i>无座位表</el-tag>
								</template>
							</el-descriptions-item>
							<el-descriptions-item>
								<template slot="label">
									<i class="el-icon-s-management"></i>
									生成座位
								</template>
								<template>
									<el-button type="primary" :disabled="hasSeats" @click="generate" :loading="generating">
										<i class="el-icon-s-promotion"></i>
										点我生成
									</el-button>
								</template>
							</el-descriptions-item>
						</el-descriptions>
					</div>
				</el-card>
			</el-main>
		</el-container>
	</div>
</template>

<script>
import $ajax from "../../ajax";
export default {
	data() {
		return {
			train: "",
			hasSeats: false,
			generating: false,
		};
	},
	methods: {
		async ifGeneratedSeats() {
			let res = await $ajax.post(
				"/admin/ifGeneratedSeats",
				{
					train: this.train,
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.hasSeats = res.data.data;
			}
		},
		async generate() {
			this.generating = true;
			let res = await $ajax.post(
				"/admin/setSeats",
				{
					train: this.train,
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.$notify({
					title: "成功",
					message: "成功生成座位表",
					type: "success",
				});
				this.hasSeats = true;
			} else {
				this.$notify({
					title: "失败",
					message: "生成座位表失败，" + res.data.data,
					type: "error",
				});
			}
			this.generating = false;
		},
	},
	created() {
		this.train = this.$route.params.trainId;
		if (localStorage.getItem("token") == null) {
			this.$router.replace("/");
		}
		this.ifGeneratedSeats();
	},
	watch: {
		$route(to) {
			this.train = to.params.trainId;
		},
	},
};
</script>