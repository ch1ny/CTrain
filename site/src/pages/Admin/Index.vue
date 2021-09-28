<template>
	<div>
		<el-container>
			<el-main>
				<el-card>
					<div slot="header" style="text-align: left">
						<span><b>后台管理</b></span>
					</div>
					<el-tabs v-model="tabPage">
						<el-tab-pane label="列车管理" name="route">
						</el-tab-pane>
						<el-tab-pane label="用户管理" name="user">
						</el-tab-pane>
						<keep-alive>
							<router-view />
						</keep-alive>
					</el-tabs>
				</el-card>
			</el-main>
		</el-container>
	</div>
</template>

<script>
export default {
	data() {
		return {
			tabPage: "route",
		};
	},
	methods: {
		adminJump(tab) {
			console.log(tab);
		},
	},
	created() {
		if (localStorage.getItem("token") == null) {
			this.$router.replace("/");
		}
	},
	watch: {
		tabPage(newVal) {
			switch (newVal) {
				case "route":
					this.$router.push({ path: "route" });
					break;
				case "user":
					this.$router.push({ path: "user" });
					break;
			}
		},
	},
};
</script>