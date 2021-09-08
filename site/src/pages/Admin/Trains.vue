<template>
	<div>
		<el-container>
			<el-main>
				<el-page-header @back="goBack">
				</el-page-header>
				<el-card style="margin-top: 15px">
					<div slot="header" style="text-align: left">
						<span><b>{{train}}次列车管理</b></span>
					</div>
					<el-tabs v-model="tabPage">
						<el-tab-pane label="沿途车站" name="first">
							<Route></Route>
						</el-tab-pane>
						<el-tab-pane label="座位管理" name="second">
							<Seats></Seats>
						</el-tab-pane>
					</el-tabs>
				</el-card>
			</el-main>
		</el-container>
	</div>
</template>

<script>
import Route from "../../components/Admin/Route.vue";
import Seats from "../../components/Admin/Seats.vue";
export default {
	components: {
		Route,
		Seats,
	},
	data() {
		return {
			train: "",
			tabPage: "first",
		};
	},
	methods: {
		goBack() {
			this.$router.go(-1);
		},
	},
	async created() {
		this.train = this.$route.params.trainId;
		if (localStorage.getItem("token") == null) {
			this.$router.replace("/");
		}
	},
	beforeCreate() {
		document.title = `${this.$route.params.trainId}次列车管理`;
	},
	watch: {
		$route(to) {
			this.train = to.params.trainId;
			document.title = `${this.train}次列车管理`;
		},
	},
};
</script>

<style scoped>
::v-deep .el-collapse-item__content {
	padding: 0%;
}
</style>